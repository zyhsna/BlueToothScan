package edu.zyh.finalproject.ui.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.enums.TravelStrategy;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviPoi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MapFragment extends Fragment {
    private TextureMapView mMapView = null;
    private static final String TAG = "MAP_FRAGMENT";

    private View root;
    private AMap aMap;


    private AMapLocationClient mLocationClient = null;

    private AMapNavi mapNavi;

    private boolean isFirstLocate = true;

    public MapFragment() {

    }



    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mLocationClient.startLocation();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.stopLocation();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_map, container, false);
        }

        mMapView = root.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);


        aMap = mMapView.getMap();
        initLocationClient();
        initMyPoint();
        initNavigation();
        startNavigation();
        return root;
    }

    /**
     * 初始化client对象
     */
    private void initLocationClient() {
        try {
            mLocationClient = new AMapLocationClient(MyApplication.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        final AMapLocationClientOption clientOption = new AMapLocationClientOption();
        //设置GPS高精度显示
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //禁止模拟定位
        clientOption.setMockEnable(false);
        //设置5s定位一次
        clientOption.setInterval(5000);
        mLocationClient.setLocationOption(clientOption);
        //开始定位
        mLocationClient.startLocation();

    }

    /**
     * 在地图上初始化持有者位置
     */
    private void initMyPoint() {
        final MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        myLocationStyle.strokeWidth(16f);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style


        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    private void initNavigation() {
        try {
            mapNavi = AMapNavi.getInstance(MyApplication.getContext());

        } catch (AMapException e) {
            e.printStackTrace();
        }

    }


    private void startNavigation() {

        // 构造起点POI
        NaviPoi start = new NaviPoi("故宫博物馆", null, "B000A8UIN8");
        // 构造终点POI
        NaviPoi end = new NaviPoi("北京大学", null, "B000A816R6");
        // 进行骑行算路
        mapNavi.calculateRideRoute(start, end, TravelStrategy.SINGLE);
    }


    private AMapLocationListener mLocationListener = mapLocation -> {
        if (mapLocation != null) {
            if (mapLocation.getErrorCode() == 0) {
                final double latitude = mapLocation.getLatitude();//获取纬度
                final double longitude = mapLocation.getLongitude();//获取经度
                final String city = mapLocation.getCity();//城市信息
                final String district = mapLocation.getDistrict();//城区信息
                final String street = mapLocation.getStreet();//街道信息
                final String streetNum = mapLocation.getStreetNum();//街道门牌号信息
                LogUtil.info(TAG, "经度:" + longitude + "纬度:" + latitude);
                LogUtil.info(TAG, city + district + street + streetNum);

                //首次打开应用更新定位
                if (isFirstLocate) {
                    updateLocation(longitude, latitude);
                    isFirstLocate = false;
                }
            } else {
                //说明出错了
                LogUtil.error(TAG, mapLocation.getErrorInfo());
            }
        }
    };

    /**
     * 用户更新位置
     *
     * @param longitude 经度
     * @param latitude  纬度
     */
    private void updateLocation(double longitude, double latitude) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", 0);
        String deviceId = MyApplication.getDeviceUID();
        String url = "/map/updateLocation";
        HashMap<String, String> params = new HashMap<>();
        params.put("deviceUID", deviceId);
        params.put("userId", String.valueOf(userId));
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }


}