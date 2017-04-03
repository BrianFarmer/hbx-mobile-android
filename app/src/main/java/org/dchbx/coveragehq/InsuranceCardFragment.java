package org.dchbx.coveragehq;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 3/30/2017.
 */

public class InsuranceCardFragment extends BrokerFragment {
    private String TAG = "InsuranceCardFragment";
    private View view;
    private UserEmployee userEmployee;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.insurance_card, null);
        init();
        getMessages().getUserEmployee();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.UpdateInsuranceCard updateInsuranceCard) {
        getMessages().getUserEmployee();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetUserEmployeeResults getUserEmployeeResults) {
        populate(getUserEmployeeResults.getUserEmployee());
    }

    private void populate(UserEmployee userEmployee) {
        if (userEmployee != null){
            this.userEmployee = userEmployee;
        } else {
            userEmployee = this.userEmployee;
        }

        Button frontCapture = (Button)view.findViewById(R.id.buttonCaptureFront);
        ImageView frontView = (ImageView) view.findViewById(R.id.imageViewFrontView);
        Button frontRemove = (Button) view.findViewById(R.id.buttonRemoveFront);
        Button frontReplace = (Button) view.findViewById(R.id.buttonReplaceFront);
        Button rearCapture = (Button)view.findViewById(R.id.buttonCaptureRear);
        ImageView rearView = (ImageView) view.findViewById(R.id.imageViewRearView);
        Button rearRemove = (Button) view.findViewById(R.id.buttonRemoveRear);
        Button rearReplace = (Button) view.findViewById(R.id.buttonReplaceRear);

        if (userEmployee.insuranceCardFrontFileName == null){
            frontCapture.setVisibility(View.VISIBLE);
            frontCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePictureFront();
                }
            });
            frontView.setVisibility(View.GONE);
            frontRemove.setVisibility(View.GONE);
            frontReplace.setVisibility(View.GONE);
        } else {
            frontCapture.setVisibility(View.GONE);
            frontView.setVisibility(View.VISIBLE);
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
                    replaceFront();
                }
            });

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(userEmployee.insuranceCardFrontFileName, options);
           frontView.setImageBitmap(bitmap);
        }

        if (userEmployee.insuranceCardRearFileName == null){
            rearCapture.setVisibility(View.VISIBLE);
            rearCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePictureRear();
                }
            });
            rearView.setVisibility(View.GONE);
            rearRemove.setVisibility(View.GONE);
            rearReplace.setVisibility(View.GONE);
        } else {
            rearCapture.setVisibility(View.GONE);
            rearView.setVisibility(View.VISIBLE);
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
                    replaceRear();
                }
            });
        }
    }

    private void replaceFront() {

    }

    private void removeFront() {

    }

    private void replaceRear() {

    }

    private void removeRear() {

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
