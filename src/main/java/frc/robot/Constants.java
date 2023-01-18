// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

  public static class DS_USB {
    public static final int LEFT_STICK = 0, RIGHT_STICK = 1, GAMEPAD = 2;

    private DS_USB() {
    }
  }

  /**
  *From 0 in the up direction, and counter clockwise in degrees (right is 90 and upper left is 315).
  */
  
  public static class JOYSTICK_BUTTONS {
    public static final int BTN1 = 1;
    public static final int BTN2 = 2;
    public static final int BTN3 = 3;
    public static final int BTN4 = 4;
    public static final int BTN5 = 5;
    public static final int BTN6 = 6;
    public static final int BTN7 = 7;
    public static final int BTN8 = 8;
    public static final int BTN9 = 9;
    public static final int BTN10 = 10;
    public static final int BTN11 = 11;

    public JOYSTICK_BUTTONS() {
    }
  }

  public static class SWERVE {

    public static final double WHEEL_DIAMETER = 0.0762; // meters
    // changes drive speed (more pinions = zoom robot)
    public static final int PINION_TEETH = 14;

    // 45 teeth on the wheel's bevel gear, 22 teeth on the first-stage spur gear, 15 teeth on the bevel pinion
    public static final double MOTOR_REDUCTION = (45.0 * 22) / (PINION_TEETH * 15);

    // conversion factor to get swerve to return rotations --> meters
    // wheel circumference (2*pi*r) / MOTOR_REDUCTION
    public static final double METERS_CONVERSION = (WHEEL_DIAMETER * Math.PI)
        / MOTOR_REDUCTION; // meters
    public static final double VELOCITY_CONVERSION = METERS_CONVERSION / 60.0; // meters per second

    public static final double RADIANS_CONVERSION = 2 * Math.PI;
    public static final double RADIANS_VELOCITY_CONVERSION = RADIANS_CONVERSION / 60;

    public static final double MAX_DIRECTION_SPEED = 5.0; //meters per second
    public static final double MAX_ROTATIONAL_SPEED = 2 * Math.PI; // radians per second
  }

  public static class UNITS {
    public static final int
      MAG_ENCODER_TICKS_PER_REVOLUTION = 4096,
      PIGEON_NATIVE_UNITS_PER_ROTATION = 8192;

    private UNITS() {
    }
}

}
