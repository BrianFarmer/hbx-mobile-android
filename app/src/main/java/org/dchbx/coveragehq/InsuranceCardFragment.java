package org.dchbx.coveragehq;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 3/30/2017.
 */

public class InsuranceCardFragment extends BrokerFragment {
    private String TAG = "InsuranceCardFragment";
    private View view;
    private UserEmployee userEmployee;
    private Uri cameraUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.insurance_card, null);
        init();
        getMessages().getUserEmployee();
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        init();
        Log.d(TAG, "resuminng insurance card fragment");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void doThis(Events.UpdateInsuranceCard updateInsuranceCard) {
        getMessages().getUserEmployee();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUserEmployeeResults getUserEmployeeResults) {
        populate(getUserEmployeeResults.getUserEmployee(), cameraUri);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.RemoveInsuraceCardImageResult removeInsuraceCardImageResult) {
        if (removeInsuraceCardImageResult.isSuccess()){
            getMessages().getUserEmployee();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.file_error)
                    .setTitle(R.string.app_title);
            AlertDialog dialog = builder.create();
        }
    }

    private void populate(UserEmployee userEmployee, Uri cameraUri) {
        if (userEmployee != null){
            this.userEmployee = userEmployee;
        } else {
            userEmployee = this.userEmployee;
        }

        RelativeLayout noImageArea = (RelativeLayout) view.findViewById(R.id.noImageArea);
        RelativeLayout relativeLayoutFrontCapture = (RelativeLayout) view.findViewById(R.id.relativeLayoutFrontCapture);
        RelativeLayout relativeLayoutRearCapture = (RelativeLayout) view.findViewById(R.id.relativeLayoutRearCapture);
        Button frontCapture = (Button)view.findViewById(R.id.buttonCaptureFront);
        ImageView frontView = (ImageView) view.findViewById(R.id.imageViewFrontView);
        Button frontRemove = (Button) view.findViewById(R.id.buttonRemoveFront);
        Button frontReplace = (Button) view.findViewById(R.id.buttonReplaceFront);
        Button rearCapture = (Button)view.findViewById(R.id.buttonCaptureRear);
        ImageView rearView = (ImageView) view.findViewById(R.id.imageViewRearView);
        Button rearRemove = (Button) view.findViewById(R.id.buttonRemoveRear);
        Button rearReplace = (Button) view.findViewById(R.id.buttonReplaceRear);
        LinearLayout frontLayout = (LinearLayout)view.findViewById(R.id.frontLayout);
        LinearLayout rearLayout = (LinearLayout)view.findViewById(R.id.rearLayout);

        if (userEmployee.insuranceCardFrontFileName == null
            && userEmployee.insuranceCardRearFileName == null){
            noImageArea.setVisibility(View.VISIBLE);
            frontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePictureFront();
                }
            });
            frontCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePictureFront();
                }
            });

            relativeLayoutFrontCapture.setVisibility(View.VISIBLE);
            relativeLayoutRearCapture.setVisibility(View.VISIBLE);

            frontView.setVisibility(View.GONE);
            frontRemove.setVisibility(View.GONE);
            frontReplace.setVisibility(View.GONE);

            rearView.setVisibility(View.GONE);
            rearRemove.setVisibility(View.GONE);
            rearReplace.setVisibility(View.GONE);
        } else {
            noImageArea.setVisibility(View.GONE);
            if (userEmployee.insuranceCardFrontFileName == null) {
                relativeLayoutFrontCapture.setVisibility(View.VISIBLE);
                frontCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureFront();
                    }
                });
                noImageArea.setVisibility(View.GONE);
                frontView.setVisibility(View.GONE);
                frontRemove.setVisibility(View.GONE);
                frontReplace.setVisibility(View.GONE);
            } else {
                frontView.setVisibility(View.VISIBLE);
                relativeLayoutFrontCapture.setVisibility(View.GONE);
                frontRemove.setVisibility(View.VISIBLE);
                frontRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeFront();
                    }
                });
                frontReplace.setVisibility(View.VISIBLE);
                frontReplace.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureFront();
                    }
                });

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(userEmployee.insuranceCardFrontFileName, options);
                frontView.setImageBitmap(bitmap);
            }

            if (userEmployee.insuranceCardRearFileName == null) {
                rearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureRear();
                    }
                });
                rearCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureRear();
                    }
                });
                noImageArea.setVisibility(View.GONE);
                relativeLayoutRearCapture.setVisibility(View.VISIBLE);
                rearView.setVisibility(View.GONE);
                rearRemove.setVisibility(View.GONE);
                rearReplace.setVisibility(View.GONE);
            } else {
                rearView.setVisibility(View.VISIBLE);
                relativeLayoutRearCapture.setVisibility(View.GONE);
                rearRemove.setVisibility(View.VISIBLE);
                rearRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeRear();
                    }
                });
                rearReplace.setVisibility(View.VISIBLE);
                rearReplace.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureRear();
                    }
                });

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(userEmployee.insuranceCardRearFileName, options);
                rearView.setImageBitmap(bitmap);
            }
        }
    }

    private void removeFront() {
        getMessages().removeInsuraceCardImage(true);
    }

    private void removeRear() {
        getMessages().removeInsuraceCardImage(false);
    }

    private void takePictureFront() {
        try {
            getMessages().capturePhoto(true);
        } catch (Throwable t){
            Log.e(TAG, "excepion posting message");
        }
    }

    private void takePictureRear() {
        getMessages().capturePhoto(false);
    }
}
