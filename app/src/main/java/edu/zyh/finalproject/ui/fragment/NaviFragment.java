package edu.zyh.finalproject.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 自定义Fragment 操作AMapNaviView
 */
public class NaviFragment extends Fragment implements AMapNaviViewListener {
    private AMapNaviView mAMapNaviView;


    public NaviFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navi, container, false);
        mAMapNaviView = (AMapNaviView) view.findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
    }


    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ProjectToken.DATA_NAME, Context.MODE_PRIVATE);
        final int userId = sharedPreferences.getInt("userId", 0);
        final int taskId = sharedPreferences.getInt("taskId", 0);
        LogUtil.error("NAVI","CANCEL");
        if (userId!=0 && taskId!=0){
            String url = "/map/updateDistance";
            final HashMap<String, String> params = new HashMap<>();
            params.put("taskId",String.valueOf(taskId));
            params.put("userId",String.valueOf(userId));
            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                }
            });
        }
        Objects.requireNonNull(this.getActivity()).finish();
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }



    @Override
    public void onNaviViewShowMode(int i) {

    }
}
