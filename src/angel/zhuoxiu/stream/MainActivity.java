package angel.zhuoxiu.stream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.mail.internet.InternetAddress;
import android.R.integer;
import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;
import angel.zhuoxiu.library.util.AndroidUtils;

public class MainActivity extends ActionBarActivity {
	static final String TAG = MainActivity.class.getSimpleName();

	TextView textView;
	EditText ipInput;
	Button btnStart, btnReceive;
	VideoView videoView;
	Camera camera;
	byte[] mCallbackBuffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.text);
		ipInput = (EditText) findViewById(R.id.input);
		btnStart = (Button) findViewById(R.id.button_start);
		btnReceive = (Button) findViewById(R.id.button_receive);
		videoView = (VideoView) findViewById(R.id.video);
		btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCamera();
			}
		});
		btnReceive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setReceive();
			}
		});
		showIp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	void showIp() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return AndroidUtils.getIpAddress();
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				textView.setText(result);
			}
		}.execute();
	}

	void setCamera() {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(videoView.getHolder());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Parameters parameters = camera.getParameters();
		int bitsPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
		int width = parameters.getPreviewSize().width;
		int height = parameters.getPreviewSize().height;
		Log.i(TAG, width + " " + height + " " + bitsPerPixel + " " + width * height * bitsPerPixel);
		final int bufferLength = width * height * bitsPerPixel;
		mCallbackBuffer = new byte[bufferLength];
		camera.setPreviewCallbackWithBuffer(new PreviewCallback() {
			long time = System.currentTimeMillis();

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				Log.i(TAG, "data=" + data.length + " " + (System.currentTimeMillis() - time));
				time = System.currentTimeMillis();
				tempData1 = data;
				mCallbackBuffer = new byte[bufferLength];
				camera.addCallbackBuffer(mCallbackBuffer);
				setSend();

			}
		});
		camera.addCallbackBuffer(mCallbackBuffer);
		camera.startPreview();
	}

	byte[] tempData1;
	DatagramSocket socket;
	DatagramPacket packet;
	boolean sending = true, receiving = true;

	void setSend() {
		new Thread() {
			public void run() {
				try {
					socket = new DatagramSocket();
					InetAddress address = InetAddress.getByName(ipInput.getText().toString());
					socket.connect(address, 12345);
					while (sending) {
						if (tempData1 != null) {
							byte[] tempData2 = tempData1;
							packet = new DatagramPacket(tempData2, 1500);
							Log.i(TAG, "sending " + tempData2.length + " byte");
							socket.send(packet);
							tempData1 = null;
							tempData2 = null;
							System.gc();
							Thread.sleep(40);

						}
					}
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
		;
	}

	void setReceive() {
		new Thread() {
			@SuppressLint("NewApi")
			public void run() {
				Integer port = 12345;
				byte[] message = new byte[1500];

				File tempFile = null;
				MediaPlayer mediaplayer = null;
				try {
					tempFile = File.createTempFile("video", ".yuv");
					mediaplayer = new MediaPlayer();
					mediaplayer.setDataSource(tempFile.getAbsolutePath());
					mediaplayer.setDisplay(videoView.getHolder());
					mediaplayer.prepare();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					DatagramSocket datagramSocket = new DatagramSocket(port);
					DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
					Log.d(TAG, "receiving start1 "+tempFile);
					while (receiving) {
						Log.d(TAG, "receiving start2");
						datagramSocket.receive(datagramPacket);
						Log.d(TAG, "receiving " + datagramPacket.getAddress().getHostAddress().toString() + ":"
								+ new String(datagramPacket.getData()));
						FileOutputStream fos = new FileOutputStream(tempFile, true);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(datagramPacket.getData(), 0, datagramPacket.getData().length);
						bos.flush();
						bos.close();
						if (!mediaplayer.isPlaying()) {
							mediaplayer.start();
						}
					}
					datagramSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
