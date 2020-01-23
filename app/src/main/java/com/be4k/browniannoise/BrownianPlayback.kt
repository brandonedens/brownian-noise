package com.be4k.browniannoise

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.IBinder
import kotlin.random.Random

class BrownianPlayback : Service() {

    private var mLastOut = 0.0f;

    private val mTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
        )
        .setBufferSizeInBytes(BrownianPlayback.AUDIO_BUF_SIZE)
        .build()

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var samplesPosition = 0;
        val samples = FloatArray(BrownianPlayback.AUDIO_BUF_SIZE) { Random.nextFloat() * 2 - 1 }
        for (i in 0 until BrownianPlayback.AUDIO_BUF_SIZE) {
            samples[i] = (this.mLastOut + (0.02f * samples[i])) / 1.02f
            this.mLastOut = samples[i]
            samples[i] *= 3.5f
        }
        samplesPosition += mTrack.write(samples, 0, BrownianPlayback.AUDIO_BUF_SIZE, AudioTrack.WRITE_BLOCKING)
        mTrack.positionNotificationPeriod = samplesPosition / 2;
        mTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onPeriodicNotification(track: AudioTrack) {
                samplesPosition += track.write(samples, samplesPosition,
                    BrownianPlayback.AUDIO_BUF_SIZE - samplesPosition, AudioTrack.WRITE_BLOCKING)
                if (samplesPosition >= BrownianPlayback.AUDIO_BUF_SIZE) {
                    samplesPosition = 0
                    // Create more data.
                    for (i in 0 until BrownianPlayback.AUDIO_BUF_SIZE) {
                        val white = Random.nextFloat() * 2 - 1;
                        samples[i] = (this@BrownianPlayback.mLastOut + (0.02f * white)) / 1.02f
                        this@BrownianPlayback.mLastOut = samples[i]
                        samples[i] *= 3.5f
                    }
                }
                samplesPosition += track.write(samples, samplesPosition,
                    BrownianPlayback.AUDIO_BUF_SIZE - samplesPosition, AudioTrack.WRITE_BLOCKING)
                track.flush()
            }
            override fun onMarkerReached(track: AudioTrack) = Unit

        })
        mTrack.play()

        return Service.START_STICKY
    }

    companion object {
        /**
         * Size of the audio buffer in samples.
         */
        private val AUDIO_BUF_SIZE = 176400;
    }
}