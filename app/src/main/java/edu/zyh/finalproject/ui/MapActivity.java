package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.ErrorCode;
import com.amap.api.track.OnTrackLifecycleListener;
import com.amap.api.track.TrackParam;
import com.amap.api.track.query.entity.LocationMode;
import com.amap.api.track.query.model.AddTerminalRequest;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.DistanceRequest;
import com.amap.api.track.query.model.DistanceResponse;
import com.amap.api.track.query.model.HistoryTrackResponse;
import com.amap.api.track.query.model.LatestPointResponse;
import com.amap.api.track.query.model.OnTrackListener;
import com.amap.api.track.query.model.ParamErrorResponse;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MapActivity extends AppCompatActivity implements AMapNaviListener, AMap.OnMapLoadedListener {
    private AMapNavi mAMapNavi;
    private AMap mAMap;
    private NaviLatLng endLatlng = null;
    private NaviLatLng startLatlng = null;
    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    private RouteOverLay mRouteOverlay;

    private List<Double> lastLocation;

    private double currentLatitude;
    private double currentLongitude;
    private AMapLocationClient mLocationClient = null;

    private int deviceId;
    private int taskId;
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<>();
    /**
     * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
     * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
     */
    int strategyFlag = 0;

    private Button mStartNaviButton;

    //轨迹监控Client对象
    private AMapTrackClient aMapTrackClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        final Intent intent = getIntent();
        deviceId = intent.getIntExtra("deviceId", 0);
        taskId = intent.getIntExtra("taskId", 0);

        aMapTrackClient = new AMapTrackClient(getApplicationContext());

        mStartNaviButton = findViewById(R.id.calculate_route_start_navi);
        mStartNaviButton.setOnClickListener(view -> startNavi());
        if (deviceId != 0) {
            getDeviceLastLocation();
        }else{
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            endLatlng = new NaviLatLng(latitude, longitude);
            endList.add(endLatlng);
            initTrackClient();
            initLocationClient();
            setUpMapIfNeeded();
            initNavi();
        }

    }

    /**
     * 初始化轨迹client
     */
    private void initTrackClient() {
        //设置缓存大小60MB
        aMapTrackClient.setCacheSize(60);
        //设置高精度定位
        aMapTrackClient.setLocationMode(LocationMode.HIGHT_ACCURACY);
        aMapTrackClient.setOnTrackListener(myTrackLifecycleListener);
        aMapTrackClient.queryTerminal(new QueryTerminalRequest(serviceId, terminalName), myTrackListener);


    }

    private long serviceId = 625616;
    private long terminalId;
    private final String terminalName= "project_track";
    private final  OnTrackListener myTrackListener = new OnTrackListener(){
        @Override
        public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
            if (queryTerminalResponse.isSuccess()) {
                if (queryTerminalResponse.getTid() <= 0) {
                    // terminal还不存在，先创建
                    aMapTrackClient.addTerminal(new AddTerminalRequest(terminalName, serviceId), new OnTrackListener() {
                        @Override
                        public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {

                        }

                        @Override
                        public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {
                            if (addTerminalResponse.isSuccess()) {
                                // 创建完成，开启猎鹰服务
                                terminalId = addTerminalResponse.getTid();
                                aMapTrackClient.startTrack(new TrackParam(serviceId, terminalId), myTrackLifecycleListener);
                            } else {
                                // 请求失败
                                Toast.makeText(MapActivity.this, "请求失败，" + addTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onDistanceCallback(DistanceResponse distanceResponse) {
                            final double distance = distanceResponse.getDistance();
                            String url = "/map/updateDistance";
                            final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
                            final int userId = sharedPreferences.getInt("userId", 0);
                            final HashMap<String, String> params = new HashMap<>(3);
                            params.put("userId", String.valueOf(userId));
                            params.put("distance", String.valueOf(distance));
                            params.put("taskId",String.valueOf(taskId));
                            HttpUtils.get(url, params, new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                                }
                            });
                        }

                        @Override
                        public void onLatestPointCallback(LatestPointResponse latestPointResponse) {

                        }

                        @Override
                        public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {

                        }

                        @Override
                        public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {

                        }

                        @Override
                        public void onAddTrackCallback(AddTrackResponse addTrackResponse) {

                        }

                        @Override
                        public void onParamErrorCallback(ParamErrorResponse paramErrorResponse) {

                        }
                    });
                } else {
                    // terminal已经存在，直接开启猎鹰服务
                    terminalId = queryTerminalResponse.getTid();
                    aMapTrackClient.startTrack(new TrackParam(serviceId, terminalId), myTrackLifecycleListener);
                }
            } else {
                // 请求失败
                Toast.makeText(MapActivity.this, "请求失败，" + queryTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {

        }

        @Override
        public void onDistanceCallback(DistanceResponse distanceResponse) {

        }

        @Override
        public void onLatestPointCallback(LatestPointResponse latestPointResponse) {

        }

        @Override
        public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {

        }

        @Override
        public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {

        }

        @Override
        public void onAddTrackCallback(AddTrackResponse addTrackResponse) {

        }

        @Override
        public void onParamErrorCallback(ParamErrorResponse paramErrorResponse) {

        }
    };

    /**
     * 自定义轨迹监听
     */
    private final OnTrackLifecycleListener myTrackLifecycleListener = new OnTrackLifecycleListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        @Override
        public void onStartGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_GATHER_SUCEE ||
                    status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                Toast.makeText(MapActivity.this, "定位采集开启成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapActivity.this, "定位采集启动异常，" + msg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStartTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_TRACK_SUCEE ||
                    status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK ||
                    status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                // 服务启动成功，继续开启收集上报
                aMapTrackClient.startGather(this);
            } else {
                Toast.makeText(MapActivity.this, "轨迹上报服务服务启动异常，" + msg, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onStopGatherCallback(int i, String s) {

        }

        @Override
        public void onStopTrackCallback(int i, String s) {

        }
    };

    /**
     * 获取设备上一次位置
     */
    private void getDeviceLastLocation() {
        String url = "/device/getDeviceLastLocation";
        final HashMap<String, String> params = new HashMap<>();
        params.put("deviceId", String.valueOf(deviceId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String string = response.body().string();
                final JSONData responseData = JSONUtil.getResponseData(string);
                final Message message = new Message();
                if (responseData.getStateCode() == 0) {
                    final Gson gson = new Gson();
                    lastLocation = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<Double>>() {
                    }.getType());
                    message.what = ProjectToken.GET_LAST_LOCATION_SUCCESS;
                } else {
                    message.what = ProjectToken.GET_LAST_LOCATION_FAIL;
                }
                handler.sendMessage(message);
            }
        });
    }

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

    private AMapLocationListener mLocationListener = mapLocation -> {
        if (mapLocation != null) {
            if (mapLocation.getErrorCode() == 0) {
                currentLatitude = mapLocation.getLatitude();//获取纬度
                currentLongitude = mapLocation.getLongitude();//获取经度
                final String city = mapLocation.getCity();//城市信息
                final String district = mapLocation.getDistrict();//城区信息
                final String street = mapLocation.getStreet();//街道信息
                final String streetNum = mapLocation.getStreetNum();//街道门牌号信息

            } else {
                //说明出错了
                LogUtil.error("MAP_ACTIVITY", mapLocation.getErrorInfo());
            }
        }
    };


    private void calculateDriveRoute() {
        try {
            strategyFlag = mAMapNavi.strategyConvert(true, false, false, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
    }

    /**
     * 导航初始化
     */
    private void initNavi() {
        startLatlng = new NaviLatLng(currentLongitude, currentLatitude);
        startList.add(startLatlng);
//        endLatlng = new NaviLatLng(lastLocation.get(0),lastLocation.get(1));
//        endLatlng = new NaviLatLng(39.90759, 116.392582);
//        endList.add(endLatlng);
        try {
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        } catch (AMapException e) {
            e.printStackTrace();
        }
        mAMapNavi.addAMapNaviListener(this);
        calculateDriveRoute();
    }

    private void setUpMapIfNeeded() {
        if (mAMap == null) {
//            FragmentManager fm = getFragmentManager();
//
//            try {
//                MapsInitializer.initialize(MapActivity.this);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            SupportMapFragment supportMapFragment = new SupportMapFragment();

            mAMap = supportMapFragment.getMap();
//            mAMap = findViewById(R.id.map);
            mAMap.setOnMapLoadedListener(this);
        }
//        if (mAMap == null) {
//
//            mAMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//            UiSettings uiSettings = mAMap.getUiSettings();
//            if (uiSettings != null) {
//                uiSettings.setRotateGesturesEnabled(false);
//            }
//            mAMap.setOnMapLoadedListener(this);
//        }
    }

    private void cleanRouteOverlay() {
        if (mRouteOverlay != null) {
            mRouteOverlay.removeFromMap();
            mRouteOverlay.destroy();
        }
    }

    /* 绘制路径规划结果
     *
     * @param path AMapNaviPath
     */
    private void drawRoutes(AMapNaviPath path) {
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        mRouteOverlay = new RouteOverLay(mAMap, path, this);
        mRouteOverlay.addToMap();
        mRouteOverlay.zoomToSpan();
    }

    private void startNavi() {

        Intent gpsintent = new Intent(getApplicationContext(), RouteNaviActivity.class);
        gpsintent.putExtra("gps", false); // gps 为true为真实导航，为false为模拟导航
        startActivity(gpsintent);
    }


    @Override
    public void onMapLoaded() {
        calculateDriveRoute();
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {
        //到达目的地点上传路程
        long curr = System.currentTimeMillis();
        DistanceRequest distanceRequest = new DistanceRequest(
                serviceId,
                terminalId,
                curr - 12 * 60 * 60 * 1000, // 开始时间
                curr,   // 结束时间
                -1  // 轨迹id，传-1表示包含散点在内的所有轨迹点
        );
        aMapTrackClient.queryDistance(distanceRequest, myTrackListener);


    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        cleanRouteOverlay();
        AMapNaviPath path = mAMapNavi.getNaviPath();
        if (path != null) {
            drawRoutes(path);
        }
        mStartNaviButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.GET_LAST_LOCATION_SUCCESS:
                    final double longitude = lastLocation.get(0);
                    final double latitude = lastLocation.get(1);
                    endLatlng = new NaviLatLng(latitude, longitude);
                    endList.add(endLatlng);
//                    endLatlng = new NaviLatLng(39.90759, 116.392582);
//                    endList.add(endLatlng);
                    initTrackClient();
                    initLocationClient();
                    setUpMapIfNeeded();
                    initNavi();
                    break;
                case ProjectToken.GET_LAST_LOCATION_FAIL:
                    break;
                default:
                    break;
            }
        }
    };

}