package com.deluxe_viper.livestreamapp.presentation.main.stream_play;

import android.content.Context;

import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

public final class StreamPlayerUtils {

    private static DataSource.Factory dataSourceFactory;
    private static RtmpDataSource.Factory rtmpSourceFactory;

    /**
     * Returns a {@link DataSource.Factory}.
     */
    public static synchronized DataSource.Factory getDataSourceFactory(Context context) {
        if (dataSourceFactory == null) {
            context = context.getApplicationContext();
            rtmpSourceFactory = new RtmpDataSource.Factory();

            dataSourceFactory =
                    new DefaultDataSource.Factory(context, rtmpSourceFactory);
        }
        return dataSourceFactory;
    }
}
