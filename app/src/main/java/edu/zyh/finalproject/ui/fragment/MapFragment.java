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
     * ?????????client??????
     */
    private void initLocationClient() {
        try {
            mLocationClient = new AMapLocationClient(MyApplication.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //????????????????????????
        mLocationClient.setLocationListener(mLocationListener);
        final AMapLocationClientOption clientOption = new AMapLocationClientOption();
        //??????GPS???????????????
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //??????????????????
        clientOption.setMockEnable(false);
        //??????5s????????????
        clientOption.setInterval(5000);
        mLocationClient.setLocationOption(clientOption);
        //????????????
        mLocationClient.startLocation();

    }

    /**
     * ????????????????????????????????????
     */
    private void initMyPoint() {
        final MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000); //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//????????????????????????????????????????????????????????????????????????????????????????????????????????????1???1???????????????????????????????????????
        myLocationStyle.strokeWidth(16f);
        aMap.setMyLocationStyle(myLocationStyle);//?????????????????????Style


        aMap.setMyLocationEnabled(true);// ?????????true?????????????????????????????????false??????????????????????????????????????????????????????false???
    }

    private void initNavigation() {
        try {
            mapNavi = AMapNavi.getInstance(MyApplication.getContext());

        } catch (AMapException e) {
            e.printStackTrace();
        }

    }


    private void startNavigation() {

        // ????????????POI
        NaviPoi start = new NaviPoi("???????????????", null, "B000A8UIN8");
        // ????????????POI
        NaviPoi end = new NaviPoi("????????????", null, "B000A816R6");
        // ??????????????????
        mapNavi.calculateRideRoute(start, end, TravelStrategy.SINGLE);
    }


    private AMapLocationListener mLocationListener = mapLocation -> {
        if (mapLocation != null) {
            if (mapLocation.getErrorCode() == 0) {
                final double latitude = mapLocation.getLatitude();//????????????
                final double longitude = mapLocation.getLongitude();//????????????
                final String city = mapLocation.getCity();//????????????
                final String district = mapLocation.getDistrict();//????????????
                final String street = mapLocation.getStreet();//????????????
                final String streetNum = mapLocation.getStreetNum();//?????????????????????
                LogUtil.info(TAG, "??????:" + longitude + "??????:" + latitude);
                LogUtil.info(TAG, city + district + street + streetNum);

                //??????????????????????????????
                if (isFirstLocate) {
                    updateLocation(longitude, latitude);
                    isFirstLocate = false;
                }
            } else {
                //???????????????
                LogUtil.error(TAG, mapLocation.getErrorInfo());
            }
        }
    };

    /**
     * ??????????????????
     *
     * @param longitude ??????
     * @param latitude  ??????
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