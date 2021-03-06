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
     * ?????????????????????
     */
    private List<NaviLatLng> wayList = new ArrayList<>();
    /**
     * ?????????????????????????????????????????????
     */
    private List<NaviLatLng> endList = new ArrayList<>();
    /**
     * strategyFlag???????????????????????????PathPlanningStrategy????????????????????????????????????PathPlanningStrategy?????????????????????
     * ???:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
     */
    int strategyFlag = 0;

    private Button mStartNaviButton;

    //????????????Client??????
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
     * ???????????????client
     */
    private void initTrackClient() {
        //??????????????????60MB
        aMapTrackClient.setCacheSize(60);
        //?????????????????????
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
                    // terminal????????????????????????
                    aMapTrackClient.addTerminal(new AddTerminalRequest(terminalName, serviceId), new OnTrackListener() {
                        @Override
                        public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {

                        }

                        @Override
                        public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {
                            if (addTerminalResponse.isSuccess()) {
                                // ?????????????????????????????????
                                terminalId = addTerminalResponse.getTid();
                                aMapTrackClient.startTrack(new TrackParam(serviceId, terminalId), myTrackLifecycleListener);
                            } else {
                                // ????????????
                                Toast.makeText(MapActivity.this, "???????????????" + addTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
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
                    // terminal???????????????????????????????????????
                    terminalId = queryTerminalResponse.getTid();
                    aMapTrackClient.startTrack(new TrackParam(serviceId, terminalId), myTrackLifecycleListener);
                }
            } else {
                // ????????????
                Toast.makeText(MapActivity.this, "???????????????" + queryTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
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
     * ?????????????????????
     */
    private final OnTrackLifecycleListener myTrackLifecycleListener = new OnTrackLifecycleListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        @Override
        public void onStartGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_GATHER_SUCEE ||
                    status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                Toast.makeText(MapActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapActivity.this, "???????????????????????????" + msg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStartTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_TRACK_SUCEE ||
                    status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK ||
                    status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                // ?????????????????????????????????????????????
                aMapTrackClient.startGather(this);
            } else {
                Toast.makeText(MapActivity.this, "???????????????????????????????????????" + msg, Toast.LENGTH_SHORT).show();
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
     * ???????????????????????????
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

    private AMapLocationListener mLocationListener = mapLocation -> {
        if (mapLocation != null) {
            if (mapLocation.getErrorCode() == 0) {
                currentLatitude = mapLocation.getLatitude();//????????????
                currentLongitude = mapLocation.getLongitude();//????????????
                final String city = mapLocation.getCity();//????????????
                final String district = mapLocation.getDistrict();//????????????
                final String street = mapLocation.getStreet();//????????????
                final String streetNum = mapLocation.getStreetNum();//?????????????????????

            } else {
                //???????????????
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
     * ???????????????
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

    /* ????????????????????????
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
        gpsintent.putExtra("gps", false); // gps ???true?????????????????????false???????????????
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
        //??????????????????????????????
        long curr = System.currentTimeMillis();
        DistanceRequest distanceRequest = new DistanceRequest(
                serviceId,
                terminalId,
                curr - 12 * 60 * 60 * 1000, // ????????????
                curr,   // ????????????
                -1  // ??????id??????-1??????????????????????????????????????????
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