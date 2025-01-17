package com.codyy.licodeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.codyy.cocolibrary.COCO;
import com.codyy.cocolibrary.COCOListener;
import com.codyy.licodeapp.apprtc.VideoStreamsView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.codyy.licodeapp.LicodeConnector.log;


/**
 * simple fragment to display two rows of video streams
 */
public class FragmentVideoChat extends Fragment {
    // TODO replace this with your servers url!
    /**
     * server url - where to request tokens
     */
    private String mTokenServerUrl = "http://10.5.223.159:3001";

    /**
     * the licode signaling engine
     */
    VideoConnectorInterface mConnector = null;
    /**
     * basic size of a video stream
     */
    Point mBasicViewSize = null;
    /**
     * the container for all the videos
     */
    VideoGridLayout mContainer = null;
    /**
     * the video streams view
     */
    VideoStreamsView mVsv = null;
    /**
     * map of stream id -> video view
     */
    ConcurrentHashMap<String, VideoStreamPlaceholder> mVideoViews = new ConcurrentHashMap<String, VideoStreamPlaceholder>();

    /**
     * click listener for the video views - will typically change the view from
     * normal to zoomed mode
     */
    private View.OnClickListener mVsvClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mContainer != null) {
                String streamId = ((VideoStreamPlaceholder) v).getStreamId();
                StreamDescriptionInterface stream = mConnector
                        .getRemoteStreams().get(streamId);
                if (stream != null) {
                    stream.setAudioActive(!stream.isAudioActive());
                }
            }
        }
    };

    /**
     * the room observer instance for this fragment
     */
    private VideoConnectorInterface.RoomObserver mRoomObserver;
    /**
     * the view to start casting video stream
     */
    private ImageView mStartCastView;

    /**
     * create or retrieve a display element for given stream - will add this to
     * the appropriate list and the container element for video streams.
     *
     * @param streamId The source of the video data.
     * @return An existing video display element, or a newly created one.
     */
    protected VideoStreamPlaceholder makeVideoView(String streamId) {
        mVsv.addStream(streamId);
        if (mVideoViews.containsKey(streamId)) {
            return mVideoViews.get(streamId);
        } else if (getActivity() != null) {
            VideoStreamPlaceholder vsp = new VideoStreamPlaceholder(
                    getActivity(), mVsv, streamId);
            vsp.setOnClickListener(mVsvClickListener);

            mVideoViews.put(streamId, vsp);
            mContainer.addView(vsp);
            return vsp;
        }

        // no activity? this is a dead fragment
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * helper function - called to prepare the video chat connector instance
     */
    private void configureConnector() {
        mConnector = new LicodeConnector();
        mConnector.init(getActivity(), "lijian");

        mRoomObserver = new VideoConnectorInterface.RoomObserver() {
            @Override
            public void onStreamRemoved(final StreamDescriptionInterface stream) {
                if (stream == null || getActivity() == null
                        || mContainer == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        String streamId = stream.isLocal() ? VideoStreamsView.LOCAL_STREAM_ID
                                : stream.getId();
                        VideoStreamPlaceholder vsp = mVideoViews.get(streamId);
                        if (vsp != null) {
                            mVideoViews.remove(streamId);
                            mContainer.removeView(vsp);
                        }
                        stream.detachRenderer();
                        mVsv.removeStream(streamId);
                        mConnector.destroy(stream);

                        if (stream.isLocal()) {
                            mStartCastView.setVisibility(mConnector
                                    .isPublishing() ? View.GONE : View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onStreamData(String message,
                                     StreamDescriptionInterface stream) {
                // ignored, for now
            }

            @Override
            public void onStreamAdded(final StreamDescriptionInterface stream) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mConnector.subscribe(stream);
                    }
                });
            }

            @Override
            public void onRoomDisconnected() {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (String key : mVideoViews.keySet()) {
                            VideoStreamPlaceholder vsp = mVideoViews.get(key);
                            if (vsp != null) {
                                mContainer.removeView(vsp);
                            }
                            mVsv.removeStream(key);
                        }
                        mVideoViews.clear();
                        mVsv.onPause();

                        if (getActivity() != null) {
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });
            }

            @Override
            public void onRoomConnected(
                    Map<String, StreamDescriptionInterface> streamList) {
                mConnector.setBandwidthLimits(84, 16);

                if (getActivity() == null) {
                    return;
                }

                // getActivity().supportInvalidateOptionsMenu();

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO: Disconnect enable?
                        if (getActivity() != null) {
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });
            }

            @Override
            public void onStreamMediaAvailable(
                    final StreamDescriptionInterface stream) {
                if (mContainer == null) {
                    return;
                }
                mContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mContainer == null) {
                            return;
                        }
                        makeVideoView(stream.getId());
                        mConnector.attachRenderer(stream, mVsv);
                    }
                });
            }

            @Override
            public void onPublishAllowed() {
                startPublish();
            }

            @Override
            public void onRequestRefreshToken() {
                startRefreshToken();
            }
        };
        mConnector.addObserver(mRoomObserver);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mConnector != null && mConnector.isConnected()
                && mContainer != null) {
            mContainer.setCollapsed(false);
            mContainer.post(new Runnable() {

                @Override
                public void run() {
                    if (getView() == null || getActivity() == null) {
                        return;
                    }
                    Map<String, StreamDescriptionInterface> streams = mConnector
                            .getRemoteStreams();
                    for (String key : streams.keySet()) {
                        StreamDescriptionInterface stream = streams.get(key);
                        if (stream != null
                                && stream.getState() != StreamDescriptionInterface.StreamState.CLOSING) {
                            mConnector.subscribe(stream);
                        }
                    }

                    if (mConnector.isPublishing()) {
                        makeVideoView(VideoStreamsView.LOCAL_STREAM_ID);
                        mConnector.attachLocalStream(mVsv);
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mConnector != null) {
            Map<String, StreamDescriptionInterface> streams = mConnector
                    .getRemoteStreams();
            for (String key : mVideoViews.keySet()) {
                StreamDescriptionInterface stream = streams.get(key);
                if (stream != null) {
                    stream.detachRenderer();
                }
            }

            mConnector.detachLocalStream();

            // TODO dk: check if detaching is necessary if we cut connection
            // entirely?
            mConnector.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mVsv.onPause();
        // mConnector.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mVsv.onResume();
        // mConnector.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        configureConnector();
        setHasOptionsMenu(true);

        View mainView = inflater.inflate(R.layout.fragment_videochat,
                container, false);

        mContainer = (VideoGridLayout) mainView
                .findViewById(R.id.videochat_grid);
        mContainer.setCollapsed(false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mContainer.setGridDimensions(9, 1);
        } else {
            mContainer.setGridDimensions(3, 3);
        }

        mVsv = new VideoStreamsView(this.getActivity());
        mContainer.addView(mVsv);
        mContainer.setVideoElement(mVsv);

        final ImageView startCastView = new ImageView(getActivity());
        mStartCastView = startCastView;
        startCastView.setImageResource(R.drawable.streamself);
        startCastView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mConnector == null || !mConnector.isConnected()) {
                    Toast.makeText(getActivity(),
                            R.string.videochatFailNotConnected,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                v.postDelayed(new Runnable() {
                    public void run() {
                        if (mConnector != null && mConnector.isConnected()) {
                            mConnector.requestPublish();
                        }
                    }
                }, 100L); // TODO dk: hardcoded delay!
                Toast toast = Toast.makeText(getActivity(),
                        R.string.videochatWaitConnecting, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
        mContainer.addView(startCastView);
        startCastView.setVisibility(mConnector.isPublishing() ? View.GONE
                : View.VISIBLE);
        startCastView.setLayoutParams(new VideoGridLayout.LayoutParams(
                Integer.MAX_VALUE));

        return mainView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        boolean connected = false;
        if (mConnector != null) {
            connected = mConnector.isConnected();
        }

        menu.findItem(R.id.action_vchat_connect).setVisible(!connected);
        menu.findItem(R.id.action_vchat_disconnect).setVisible(connected);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_vchat_connect:
                startVideoChat();
                break;
            case R.id.action_vchat_disconnect:
                mConnector.disconnect();
                mContainer.setCollapsed(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * instantiate a new fragment
     */
    public static FragmentVideoChat createInstance(Bundle extras) {
        FragmentVideoChat result = new FragmentVideoChat();
        result.setArguments(extras);
        return result;
    }

    @Override
    public void onDestroy() {
        COCO.getDefault().unbind(getActivity());
        super.onDestroy();
        stopTimer();
        if (mConnector != null) {
            mConnector.removeObserver(mRoomObserver);
        }

    }

    /**
     * publish
     */
    protected void startPublish() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnector != null && mConnector.isConnected()) {
                    if (mContainer != null) {
                        /* VideoStreamPlaceholder placeholder = */
                        makeVideoView(VideoStreamsView.LOCAL_STREAM_ID);
                    }

                    mStartCastView.setVisibility(View.GONE);

                    mConnector.publish(mVsv);
                }
            }
        });
    }

    /**
     * request a new token - to initialize first connection
     */
    private void startVideoChat() {
        // begin connection process
        FutureResult<String> response = new FutureResult<String>();
        response.addResultObserver(new FutureResult.OnResultCallback<String>() {
            @Override
            public void OnResult(String result) {
//				mConnector.connect(result);
                try {
                    final JSONObject jsonToken = new JSONObject(result);
                    token = jsonToken;
                    String host = jsonToken.getString("host");
                    if (!host.startsWith("ws://")) {
                        host = "ws://" + host + "/socket.io/?EIO=3&transport=websocket";
                    }
                    createSocket(host);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        new XmlHttpRequest(response).execute(mTokenServerUrl);
    }

    private JSONObject token;
    // 初始化定时器
    Timer timer = new Timer();

    // 停止定时器
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            // 一定设置为null，否则定时器不会被回收
            timer = null;
        }
    }

    private void createSocket(String host) {
        COCO.getDefault().bind(getActivity(), host, new COCOListener() {
            @Override
            public void onOpen() {
                Log.i("onMessage", "coco opened");
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
//                        Log.e("lzp", "timer excute");
                        COCO.getDefault().post("2");
                    }
                }, 25000L, 25000L);
            }

            @Override
            public void onMessage(String message) {
                Log.i("onMessage", message);
                if (message.contains("sid")) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("singlePC", false);
                        object.put("token", token);

                        JSONArray arg = new JSONArray();
                        arg.put("token");
                        arg.put(object);
                        COCO.getDefault().post("420" + arg.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("onClose", code + reason);//10086
            }

            @Override
            public void onError(Exception e) {
                Log.e("error", e.getMessage());
            }
        });
    }

    /**
     * decodes a video token into a string which can then be turned into a json
     * object, returns null on errors
     */
    private static final String decodeToken(String result) {
        try {
            String token = new String(Base64.decode(result.getBytes(),
                    Base64.DEFAULT), "UTF-8");
            log("Licode token decoded: " + token);
            return token;
        } catch (UnsupportedEncodingException e) {
            log("Failed to decode token: " + e.getMessage());
        }
        return null;
    }

    /**
     * request a new token - to keep connected
     */
    private void startRefreshToken() {
        FutureResult<String> response = new FutureResult<String>();
        response.addResultObserver(new FutureResult.OnResultCallback<String>() {
            @Override
            public void OnResult(String result) {
                mConnector.refreshVideoToken(result);
            }
        });
        new XmlHttpRequest(response).execute(mTokenServerUrl);
    }

    /**
     * helper class - fake xml http request
     */
    private static class XmlHttpRequest extends AsyncTask<String, Void, String> {
        FutureResult<String> mResponse;

        public XmlHttpRequest(FutureResult<String> futureResponse) {
            mResponse = futureResponse;
        }

        @Override
        protected String doInBackground(String... params) {
            String response = null;
            HttpURLConnection conn = null;
            InputStream is = null;
            OutputStream os = null;
            try {
                URL url = new URL(params[0] + "/createToken/");
                String message = "{";
                message += "\"username\": \"lijian\", ";
                message += "\"role\": \"viewer\",  ";
                message += "\"room\": \"basicExampleRoom\",  ";
                message += "\"type\": \"erizo\",  ";
                message += "\"mediaConfiguration\": \"default\" ";
                message += " }";
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(message.getBytes().length);

                if (Build.VERSION.SDK_INT > 13) {
                    conn.setRequestProperty("Connection", "close");
                }

                conn.setRequestProperty("Content-Type",
                        "application/json;charset=utf-8");
                conn.setRequestProperty("X-Request-With", "XMLHttpRequest");

                conn.connect();

                os = conn.getOutputStream();
                os.write(message.getBytes());
                os.flush();

                is = conn.getInputStream();
                StringBuffer sb = new StringBuffer();
                int ch = -1;
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }
                response = sb.toString();

                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return decodeToken(response);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("result:", result);
            if (mResponse != null) {
                mResponse.setResult(result);
                mResponse = null;
            }
        }
    }

}
