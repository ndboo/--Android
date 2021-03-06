package com.ndboo.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ndboo.adapter.WineAdapter;
import com.ndboo.base.BaseFragment;
import com.ndboo.bean.WineBean;
import com.ndboo.net.RetrofitHelper;
import com.ndboo.utils.SharedPreferencesUtil;
import com.ndboo.wine.MainActivity;
import com.ndboo.wine.R;
import com.ndboo.wine.WineDetailActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Li on 2017/1/7.
 * 商城四种酒类Fragment
 */

public class WineFragment extends BaseFragment {
    private static final String IS_FIRST_FRAGMENT = "isFirstFragment";
    private static final String WINE_TYPE = "wineType";
    @BindView(R.id.list_view_wine)
    ListView mListViewWine;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_refresh)
    TextView mTvRefresh;

    private WineAdapter mWineAdapter;
    private List<WineBean> mWineBeen;
    private String mWineType;

    public static WineFragment newInstance(boolean isFirstFragment, String wineType) {

        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FIRST_FRAGMENT, isFirstFragment);
        bundle.putString(WINE_TYPE, wineType);
        WineFragment fragment = new WineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wine;
    }

    @Override
    public void showContent() {
        super.showContent();
        mWineBeen = new ArrayList<>();
        mWineAdapter = new WineAdapter(getActivity(), this);
        mListViewWine.setAdapter(mWineAdapter);
        mListViewWine.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WineDetailActivity.class);
                intent.putExtra("wineId", mWineBeen.get(position).getProductId());
                startActivityForResult(intent, 1);
            }
        });


    }

    private void startLoading(){
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        if (mTvRefresh != null) {
            mTvRefresh.setVisibility(View.GONE);

        }
    }

    private void loadSuccess(){
        mListViewWine.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTvRefresh.setVisibility(View.GONE);
    }

    private void loadFailure(){
        mListViewWine.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTvRefresh.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {

            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.turnToShoppingCar();
        }
        if (resultCode == 3) {
            String wineType = getArguments().getString(WINE_TYPE);
            showWinesByType(wineType, SharedPreferencesUtil.getUserId(getContext()));
        }
    }

    @Override
    protected void visibleDeal() {
        super.visibleDeal();
        mWineType = getArguments().getString(WINE_TYPE);
        showWinesByType(mWineType, SharedPreferencesUtil.getUserId(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            String wineType = getArguments().getString(WINE_TYPE);
            showWinesByType(wineType, SharedPreferencesUtil.getUserId(getContext()));
        }

    }

    private void showWinesByType(String wineType, String userId) {
        unSubscribe();
        startLoading();
        Subscription subscription = RetrofitHelper.getApi()
                .showWinesByType(wineType, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<WineBean>>() {
                    @Override
                    public void call(List<WineBean> wineBeen) {
                        loadSuccess();
                        mWineBeen = wineBeen;
                        mWineAdapter.setWines(wineBeen);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        loadFailure();
                    }
                });
        addSubscription(subscription);
    }

    @Override
    protected void inVisibleDeal() {
        super.inVisibleDeal();
        unSubscribe();
    }

    @OnClick(R.id.tv_refresh)
    public void onClick() {
        showWinesByType(mWineType, SharedPreferencesUtil.getUserId(getContext()));
    }
}
