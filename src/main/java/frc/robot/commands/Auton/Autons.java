package frc.robot.commands.Auton;

import java.util.List;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import frc.robot.subsystems.drivetrain.Drivetrain;

public class Autons {
    private SendableChooser<KnownPoses> autonChooser;
    private SendableChooser<Pose2d> startingPoseChooser;
    private KnownPoses currentSelectedAuton;
    private Drivetrain drivetrain;
    private TrajectoryConfig trajConfig;
    private ProfiledPIDController turningPIDController;
    private PIDController xController, yController;
    private Command swerveCommand;
    private boolean canSeeTarget;
    private KnownLocations knownLocations;

    private final double TURNING_P_VAL = 1;
    private final double X_P_VAL = 1, Y_P_VAL = 1;
    private final TrapezoidProfile.Constraints trapezoidalConstraint;
    private final double MAX_DIRECTIONAL_SPEED = 3, MAX_ACCELERATION = 3;
    private final double MAX_ROTATIONAL_SPEED = Math.PI;

    public Autons(Drivetrain drivetrain) {

        this.knownLocations = new KnownLocations();
        this.canSeeTarget = drivetrain.isTargetPresent();
        this.currentSelectedAuton = KnownPoses.DEFAULT;
        this.drivetrain = drivetrain;
        
        this.trajConfig = new TrajectoryConfig(MAX_DIRECTIONAL_SPEED, MAX_ACCELERATION).setKinematics(this.drivetrain.getKinematics());
        this.trapezoidalConstraint = new TrapezoidProfile.Constraints(
            MAX_ROTATIONAL_SPEED, MAX_ROTATIONAL_SPEED);

        turningPIDController = new ProfiledPIDController(TURNING_P_VAL, 0, 0, this.trapezoidalConstraint);
        turningPIDController.enableContinuousInput(-Math.PI, Math.PI);
        xController = new PIDController(X_P_VAL, 0, 0);
        yController = new PIDController(Y_P_VAL, 0, 0);
        
        autonChooser = new SendableChooser<KnownPoses>();
        autonChooser.setDefaultOption("DEFAULT", KnownPoses.DEFAULT);
        autonChooser.addOption("GRID", KnownPoses.GRID);
        autonChooser.addOption("CHARGING STATION", KnownPoses.CHARGING_STATION);
        SmartDashboard.putData("Auton Chooser", autonChooser);
        SmartDashboard.putString("Auton Selected: ", this.currentSelectedAuton.toString());

        this.startingPoseChooser = new SendableChooser<Pose2d>();
        this.startingPoseChooser.setDefaultOption("TOPMOST", knownLocations.START_TOPMOST);
        this.startingPoseChooser.addOption("TOP SECOND", knownLocations.START_TOP_SECOND);
        this.startingPoseChooser.addOption("BOTTOM SECOND", knownLocations.START_BOTTOM_SECOND);
        this.startingPoseChooser.addOption("BOTTOMMOST", knownLocations.START_BOTTOMMOST);
        SmartDashboard.putData("Manual Starting Pose", startingPoseChooser);
        SmartDashboard.putBoolean("MANUAL START NEEDED", false);

        this.swerveCommand = generateSwerveCommand();

    }

    public Command getAutonCommand() {
        // run once at the start of auton

        // will eventually use this.swerveCommand 
        // in conjunction w/ other subsystems
        // to build full autons
        return testSwerveCommand();
    }

    public Trajectory generateTestTrajectory() {
        return TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0.0)), 
            List.of(new Translation2d(.5, .5)), 
            new Pose2d(1, 1, Rotation2d.fromDegrees(180)), 
            trajConfig);
    }


    public Command testSwerveCommand() {
       // drivetrain.setManualPose(new Pose2d(0, 0, Rotation2d.fromDegrees(0)));
        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, Rotation2d.fromDegrees(0.0)), 
            List.of(new Translation2d(.5, .5)), 
            new Pose2d(1, 1, Rotation2d.fromDegrees(180)), 
            trajConfig);

        SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(
            trajectory,
            () -> drivetrain.getPose(),
            drivetrain.getKinematics(), 
            xController, 
            yController,
            turningPIDController,
            (x) -> drivetrain.setModuleStates(x),
            drivetrain);

        return swerveControllerCommand;
    }

    /** Generate the swerve-specfic command by building the desired trajectory */
    public Command generateSwerveCommand() {
        Pose2d initialPose;
        
        // if photonvision does not see a target
        // we will manually need to set the Pose using a known location from SD
        if (!this.canSeeTarget) {
            SmartDashboard.putBoolean("MANUAL START NEEDED", true);
            initialPose = startingPoseChooser.getSelected();
        } else {
            SmartDashboard.putBoolean("MANUAL START NEEDED", false);
            initialPose = drivetrain.getInitialPose();
        }
        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(
            initialPose, 
            List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
            this.currentSelectedAuton.pose,
            trajConfig);
       // drivetrain.setManualPose(trajectory.getInitialPose());
        SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(
            trajectory,
            () -> drivetrain.getPose(), // Functional interface to feed supplier
            drivetrain.getKinematics(),
            // Position controllers
            xController,
            yController,
            turningPIDController,
            (x) -> drivetrain.setModuleStates(x),
            drivetrain);
        return swerveControllerCommand;
    }

    /** Move straight in x direction */
    public Command driveStraight(double distanceX) {
        Pose2d initPose = drivetrain.getPose();
        Pose2d finalPose = new Pose2d(initPose.getX() + distanceX, initPose.getY(), initPose.getRotation());
        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(
            initPose, 
            List.of(), 
            finalPose, 
            trajConfig);

        return new SwerveControllerCommand(
            trajectory,
            () -> drivetrain.getPose(), 
            drivetrain.getKinematics(), 
            xController, 
            yController, 
            turningPIDController,
            (x) -> drivetrain.setModuleStates(x),
            drivetrain
            );
            
        }

    public void updateDash() {
        // run constantly when disabled
        KnownPoses currAuton = autonChooser.getSelected();
        boolean targetIsPresent = drivetrain.isTargetPresent();
        
        if (currAuton != this.currentSelectedAuton) {
            this.currentSelectedAuton = currAuton;
            SmartDashboard.putString("Auton Selected: ", this.currentSelectedAuton.toString());
            this.swerveCommand = generateSwerveCommand();
        }

        if (targetIsPresent != this.canSeeTarget) {
            this.canSeeTarget = targetIsPresent;
            this.swerveCommand = generateSwerveCommand();
        }
    }

    /** Destination Poses to swerve to */
    public enum KnownPoses {
        DEFAULT(new Pose2d(0, 0, new Rotation2d(0))),
        GRID(new Pose2d(0, 0, Rotation2d.fromDegrees(0))),
        CHARGING_STATION(new Pose2d(0, 0, Rotation2d.fromDegrees(0)));
        
        public final Pose2d pose;

        private KnownPoses(Pose2d pose) {
            this.pose = pose;
        }
    }

    
}
