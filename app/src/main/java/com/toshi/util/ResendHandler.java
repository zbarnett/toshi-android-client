package com.toshi.util;

import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.toshi.R;
import com.toshi.model.local.User;
import com.toshi.model.sofa.Payment;
import com.toshi.model.sofa.PaymentRequest;
import com.toshi.view.adapter.listeners.OnUpdateListener;
import com.toshi.view.fragment.DialogFragment.PaymentConfirmationDialog;

public class ResendHandler {

    private AppCompatActivity activity;

    public ResendHandler(final AppCompatActivity activity) {
        this.activity = activity;
    }

    public void showResendPaymentConfirmationDialog(final User receiver,
                                                    final Payment payment,
                                                    final OnUpdateListener listener) {
        final PaymentConfirmationDialog dialog =
                PaymentConfirmationDialog.newInstanceToshiPayment(
                        receiver.getToshiId(),
                        payment.getValue(),
                        null,
                        PaymentType.TYPE_SEND
                );

        dialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
        dialog.setOnPaymentConfirmationApprovedListener(__ -> listener.onUpdate());
    }

    public void showPaymentConfirmationDialog(final User receiver,
                                              final String amount,
                                              final OnUpdateListener listener) {
        final PaymentConfirmationDialog dialog =
                PaymentConfirmationDialog.newInstanceToshiPayment(
                        receiver.getToshiId(),
                        amount,
                        null,
                        PaymentType.TYPE_SEND
                );
        dialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
        dialog.setOnPaymentConfirmationApprovedListener(__ ->
                listener.onUpdate()
        );
    }

    public void showPaymentRequestConfirmationDialog(final User receiver,
                                                     final PaymentRequest paymentRequest,
                                                     final OnUpdateListener listener) {
        final PaymentConfirmationDialog dialog =
                PaymentConfirmationDialog.newInstanceToshiPayment(
                        receiver.getToshiId(),
                        paymentRequest.getValue(),
                        null,
                        PaymentType.TYPE_REQUEST
                );
        dialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
        dialog.setOnPaymentConfirmationApprovedListener(__ -> {
            listener.onUpdate();
        });
    }

    public void showResendDialog(final OnUpdateListener listener,
                                 final OnUpdateListener deleteListener) {
        final BottomSheetDialog resendDialog = new BottomSheetDialog(this.activity);
        final View sheetView = this.activity.getLayoutInflater().inflate(R.layout.view_chat_resend, null);
        final LinearLayout deleteView = sheetView.findViewById(R.id.deleteMessage);
        deleteView.setOnClickListener(view -> {
            resendDialog.dismiss();
            deleteListener.onUpdate();
        });
        final LinearLayout retryView = sheetView.findViewById(R.id.retry);
        retryView.setOnClickListener(__ -> {
            resendDialog.dismiss();
            listener.onUpdate();
        });
        resendDialog.setContentView(sheetView);
        resendDialog.show();
    }
}
