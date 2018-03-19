package com.example.androidthings.myproject;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.androidthings.myproject.HttpdServer.OnFireTriggerListener;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.Button.OnButtonEventListener;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManager. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManager manager = PeripheralManager.getInstance();
 * mLedGpio = manager.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity
    extends Activity
    implements OnFireTriggerListener
{

  private static final String TAG = "Things";

  private Gpio redLed;

  private Button buttonA;

  private Gpio blueLed;

  private Pwm pwm;

  private Servo servo;

  private Button buttonB;

  private Button buttonC;

  private HttpdServer httpdserver;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "Android Things project is ready!");

    // In your onCreate()
    httpdserver = new HttpdServer(this);

    try
    {
      httpdserver.start();
      initRedLed();
      blueLed = RainbowHat.openLedBlue();
      blinkLed();

      initButtonA();
      initBuzzer();

      initServo();

      initButtonB();
      initButtonC();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  @Override
  public void onFireTriggered()
  {
    try
    {
      servoToMaximum();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void onSetTriggered()
  {
    try
    {
      servoToMiddle();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void initRedLed()
      throws IOException
  {
    redLed = RainbowHat.openLedRed();
    redLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    redLed.setValue(true);
  }

  private void initButtonB()
      throws IOException
  {
    buttonB = RainbowHat.openButtonB();
    buttonB.setOnButtonEventListener(new OnButtonEventListener()
    {
      @Override
      public void onButtonEvent(Button button, boolean pressed)
      {
        try
        {
          servoToMaximum();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  private void initButtonC()
      throws IOException
  {
    buttonC = RainbowHat.openButtonC();
    buttonC.setOnButtonEventListener(new OnButtonEventListener()
    {
      @Override
      public void onButtonEvent(Button button, boolean pressed)
      {
        try
        {
          servoToMinimum();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  private void initButtonA()
      throws IOException
  {
    buttonA = new Button("BCM21", Button.LogicState.PRESSED_WHEN_LOW);
    // Then, you listen for pressed events
    buttonA.setOnButtonEventListener(new Button.OnButtonEventListener()
    {
      @Override
      public void onButtonEvent(Button button, boolean pressed)
      {
        Log.d(TAG, "Button A pressed:" + pressed);
        try
        {
          blueLed.setValue(pressed);
          if (pressed)
          {
            buzz();
          }
          servoToMiddle();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  private void servoToMiddle()
      throws IOException
  {
    servo.setAngle(0);
  }

  private void servoToMinimum()
      throws IOException
  {
    servo.setAngle(servo.getMinimumAngle());
  }

  private void servoToMaximum()
      throws IOException
  {
    servo.setAngle(servo.getMaximumAngle());
  }

  private void initServo()
      throws IOException
  {
    servo = new Servo("PWM0");
    //    servo.setPulseDurationRange(1, 2);
    servo.setPulseDurationRange(0.8, 2.2);
    servo.setAngleRange(-90, 90);
    servo.setEnabled(true);
  }

  private void buzz()
      throws IOException, InterruptedException
  {
    // Play a note
    int frequency = 440;
    pwm.setPwmFrequencyHz(frequency);
    pwm.setEnabled(true);

    // Wait
    Thread.sleep(500);

    // Stop playing the note
    pwm.setEnabled(false);
  }

  private void initBuzzer()
      throws IOException
  {
    // Initialize the buzzer
    PeripheralManager manager = PeripheralManager.getInstance();
    pwm = manager.openPwm("PWM1");
    pwm.setPwmDutyCycle(50.0); // square wave
  }

  private void blinkLed()
  {
    final HandlerThread funkyLedThread = new HandlerThread("led-thread");
    funkyLedThread.start();
    final Handler funkyLedHandler = new Handler(funkyLedThread.getLooper());
    funkyLedHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          while (true)
          {
            redLed.setValue(!redLed.getValue());
            Thread.sleep(1000);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    // Free / Close your resources here
    try
    {
      redLed.close();
      blueLed.close();
      buttonA.close();
      pwm.close();
      servo.close();
      buttonB.close();
      buttonC.close();
      httpdserver.stop();
    }
    catch (IOException e)
    {
      Log.w("DEBUG", "Unable to close resources", e);
    }
  }
}
