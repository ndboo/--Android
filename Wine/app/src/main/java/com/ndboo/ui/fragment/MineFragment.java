package com.ndboo.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ndboo.base.BaseFragment;
import com.ndboo.widget.CircleImageView;
import com.ndboo.widget.ImgTextView;
import com.ndboo.widget.PhonePopupWindow;
import com.ndboo.widget.TopBar;
import com.ndboo.wine.OrderListActivity;
import com.ndboo.wine.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Li on 2016/12/26.
 * “我的”界面
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {
    protected static final int CHOOSE_PICTURE = 0;//选择本地图片
    protected static final int TAKE_PICTURE = 1;//照相
    private static final int CROP_SMALL_PICTURE = 2;//裁剪
    protected static Uri tempUri;

    @BindView(R.id.mine_topbar)
    TopBar mTopBar;//头部

    @BindView(R.id.mine_portrait)
    CircleImageView mPortraitImageView;//头像
    @BindView(R.id.mine_nickname)
    TextView mNickNameTextView;//昵称

    @BindView(R.id.mine_order)
    ImgTextView mOrderImgTextView;//我的订单
    @BindView(R.id.mine_address)
    ImgTextView mAddressImgTextView;//收货地址
    @BindView(R.id.mine_collection)
    ImgTextView mCollectionImgTextView;//我的收藏
    @BindView(R.id.mine_service)
    ImgTextView mServiceImgTextView;//联系客服
    @BindView(R.id.mine_aboutus)
    ImgTextView mAboutUsImgTextView;//关于我们
    @BindView(R.id.mine_suggestion)
    ImgTextView mSuggestionImgTextView;//意见反馈

    //客服电话弹框
    private PhonePopupWindow mPopupWindow;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    public void showContent() {
        super.showContent();
        addListener();
    }

    private void addListener() {
        mPortraitImageView.setOnClickListener(this);
        mNickNameTextView.setOnClickListener(this);
        mOrderImgTextView.setOnClickListener(this);
        mAddressImgTextView.setOnClickListener(this);
        mCollectionImgTextView.setOnClickListener(this);
        mServiceImgTextView.setOnClickListener(this);
        mAboutUsImgTextView.setOnClickListener(this);
        mSuggestionImgTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mine_portrait:
                showChoosePicDialog();
                break;
            case R.id.mine_nickname:
                Toast.makeText(getActivity(), "昵称", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_order:
                Intent orderIntent = new Intent(getActivity(), OrderListActivity.class);
                startActivity(orderIntent);
                break;
            case R.id.mine_address:
                Toast.makeText(getActivity(), "收货地址", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_collection:
                Toast.makeText(getActivity(), "我的收藏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_service:
                if (mPopupWindow == null) {
                    mPopupWindow = new PhonePopupWindow(getActivity());
                    mPopupWindow.setMessageText("是否拨打客服电话?");
                    mPopupWindow.setOnPopupWindowClickListener(new PhonePopupWindow.OnPopupWindowClickListener() {
                        @Override
                        public void cancleClicked(View view) {
                        }

                        @Override
                        public void ensureClicked(View view) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            Uri data = Uri.parse("tel:" + "051266155111");
                            intent.setData(data);
                            startActivity(intent);
                        }
                    });
                    mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            setBackgroundAlpha(getActivity(), 1f);
                        }
                    });
                }
                mPopupWindow.showAtLocation(view, Gravity.CENTER,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                setBackgroundAlpha(getActivity(), 0.5f);
                break;
            case R.id.mine_aboutus:
                Toast.makeText(getActivity(), "关于我们", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_suggestion:
                Toast.makeText(getActivity(), "意见反馈", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 显示修改头像的对话框
     */
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("设置店标");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE:
                        // 选择本地照片
                        Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE:
                        // 拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                "image.jpg"));
                        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，
                        // 每次拍照后这个图片都会被替换
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Log.e("my", "uri=" + uri);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            mPortraitImageView.setImageBitmap(bitmap);
            //将Bitmap转换成字节流
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            try {
                byteArrayOutputStream.close();
                byte[] buffer = byteArrayOutputStream.toByteArray();
                //将图片的字节流数据加密成base64字符输出
                String photo = Base64.encodeToString(buffer, 0, buffer.length, Base64.DEFAULT);
//                uploadLogo(photo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置屏幕的背景透明度
     */
    public void setBackgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }
}
