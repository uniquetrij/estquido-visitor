package com.infy.stg.estquido.visitor.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.infy.stg.estquido.visitor.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QRScannerActivity extends AppCompatActivity implements BarcodeCallback {

    private static final String TAG = QRScannerActivity.class.getSimpleName();
    private DecoratedBarcodeView vScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        vScanner = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        vScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        vScanner.initializeFromIntent(getIntent());
        vScanner.decodeSingle(this);
    }

    @Override
    public void barcodeResult(final BarcodeResult result) {
        Log.d(TAG, result.toString());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("SCAN_RESULT", result.toString());
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        vScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vScanner.pause();
    }

}
