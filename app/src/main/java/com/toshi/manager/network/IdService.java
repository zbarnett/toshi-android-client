/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.manager.network;


import android.content.Context;

import com.squareup.moshi.Moshi;
import com.toshi.manager.network.interceptor.LoggingInterceptor;
import com.toshi.manager.network.interceptor.OfflineCacheInterceptor;
import com.toshi.manager.network.interceptor.ReadFromCacheInterceptor;
import com.toshi.manager.network.interceptor.SigningInterceptor;
import com.toshi.manager.network.interceptor.AppInfoUserAgentInterceptor;
import com.toshi.model.adapter.RealmListAdapter;
import com.toshi.R;
import com.toshi.view.BaseApplication;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class IdService {

    private static IdService instance;

    private final IdInterface idInterface;
    private final OkHttpClient.Builder client;
    private final Cache cache;

    public IdInterface getApi() {
        return idInterface;
    }

    public static IdService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized IdService getSync() {
        if (instance == null) {
            instance = new IdService();
        }
        return instance;
    }

    public IdService(final IdInterface idInterface, final Context context) {
        this.cache = buildCache(context);
        this.client = buildClient(cache);
        this.idInterface = idInterface;
    }

    private IdService() {
        this.cache = buildCache(BaseApplication.get());
        this.client = buildClient(cache);
        final Retrofit retrofit = buildRetrofit(client);
        this.idInterface = retrofit.create(IdInterface.class);
    }

    private OkHttpClient.Builder buildClient(final Cache cache) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient
                .Builder()
                .cache(cache)
                .addNetworkInterceptor(new ReadFromCacheInterceptor())
                .addInterceptor(new OfflineCacheInterceptor())
                .addInterceptor(new AppInfoUserAgentInterceptor())
                .addInterceptor(new SigningInterceptor());

        addLogging(clientBuilder);
        return clientBuilder;
    }

    private Cache buildCache(final Context context) {
        final File cachePath = new File(context.getCacheDir(), "idCache");
        return new Cache(cachePath, 1024 * 1024 * 2);
    }

    private void addLogging(final OkHttpClient.Builder builder) {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
    }

    private Retrofit buildRetrofit(final OkHttpClient.Builder clientBuilder) {
        final Moshi moshi = new Moshi.Builder()
                .add(new RealmListAdapter())
                .build();

        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        return new Retrofit.Builder()
                .baseUrl(BaseApplication.get().getResources().getString(R.string.id_url))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(clientBuilder.build())
                .build();
    }

    public void clearCache() throws IOException {
        this.cache.evictAll();
    }
}
