package com.toshi.presenter.factory;

import com.toshi.presenter.PaymentRequestConfirmPresenter;

public class PaymentRequestConfirmPresenterFactory implements PresenterFactory<PaymentRequestConfirmPresenter> {
    @Override
    public PaymentRequestConfirmPresenter create() {
        return new PaymentRequestConfirmPresenter();
    }
}
