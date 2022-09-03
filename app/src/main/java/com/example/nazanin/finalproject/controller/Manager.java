package com.example.nazanin.finalproject.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.nazanin.finalproject.activity.MainActivity;
import com.example.nazanin.finalproject.model.DTO.Lte;

import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;


public class Manager {
    private TelephonyManager tm;
    private Context context;
    private int cellId;
    public static int coverage;
    private Lte lte = new Lte();

    public Manager(Context context, TelephonyManager tm) {
        this.context = context;
        this.tm = tm;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startScan() {

        tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        //get data every 5 seconds
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {
//
                @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = tm.getAllCellInfo();
                for (CellInfo info : cellInfoList) {
                    if (info instanceof CellInfoGsm) {
                        if (info.isRegistered()) {
                            final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                            cellId = identityGsm.getCid();
                            identityGsm.getLac();
                            String PLMN = String.valueOf(identityGsm.getMcc()) + String.valueOf(identityGsm.getMnc()); //plmn = mcc+mnc
                        }
                        giveGSMPowerParameters(info);
                    }

                    // get lte data
                    else if (info instanceof CellInfoLte) {

                        //serving cell information
                        if (info.isRegistered()) {
                            final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                            cellId = identityLte.getCi(); //cell identity
                            int TAC = identityLte.getTac(); //tracking area code
                            String PLMN = String.valueOf(identityLte.getMcc()) + String.valueOf(identityLte.getMnc()); //plmn = mcc+mnc

                        }

                        giveLtePowerParameters(info);


                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                            && info instanceof CellInfoWcdma) {
                        if (info.isRegistered()) {
                            final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                            cellId = identityWcdma.getCid();
                            identityWcdma.getLac();
                            String PLMN = String.valueOf(identityWcdma.getMcc()) + String.valueOf(identityWcdma.getMnc());

                        }
                        giveUMTSPowerParameters(info);

                    } else {


                    }
                }

                }
                else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, MainActivity.PERMISSION_READ_STATE);
                }

                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void giveLtePowerParameters(final CellInfo info){

        CellSignalStrengthLte lteInfo = ((CellInfoLte) info).getCellSignalStrength();
        lte.setRsrp(lteInfo.getRsrp());  //RSRP
        lte.setRsrq(lteInfo.getRsrq());  //RSRQ
        lte.setSinr(lteInfo.getRssnr()); //SINR
//        if (Build.VERSION.SDK_INT >= 29) {
//            lte.setRssi(lteInfo.getRssi()); //RSSI
//        }

        coverage = (lte.getRsrp()+lte.getRsrq()+lte.getSinr())/3;
      //  MainActivity.coverage = coverage;


        Toast.makeText(context, "rsrp"+lte.getRsrp()+"  "+lte.getRsrq()+"  "+lte.getSinr()
                +"ave  "+coverage,
                Toast.LENGTH_SHORT).show();

    }

    private void giveGSMPowerParameters(final CellInfo info){
        CellSignalStrengthGsm gsmInfo = ((CellInfoGsm) info).getCellSignalStrength();
        gsmInfo.getDbm(); // rxlev


        Toast.makeText(context, "rsrp"+gsmInfo.getDbm()+"  "+gsmInfo.getLevel()+"  "+
                        "   "+gsmInfo.getAsuLevel(),
                Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void giveUMTSPowerParameters(final CellInfo info){
        CellSignalStrengthWcdma umtsInfo = ((CellInfoWcdma) info).getCellSignalStrength();
        umtsInfo.getDbm(); //RSCP
//        if (Build.VERSION.SDK_INT >= 30) {
//            umtsInfo.getEcNo(); //EC/N0
//        }
    }

}
