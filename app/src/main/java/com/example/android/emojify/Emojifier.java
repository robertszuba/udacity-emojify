package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

  private static final double SMILING_PROB_THRESHOLD = .15;
  private static final double EYE_OPEN_PROB_THRESHOLD = .5;

  public static void detectFaces(final Context context, final Bitmap bitmap) {
    final FaceDetector faceDetector = new FaceDetector.Builder(context)
            .setMode(FaceDetector.ACCURATE_MODE)
            .setTrackingEnabled(false)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build();

    final boolean operational = faceDetector.isOperational();

    final Frame frame = new Frame.Builder().setBitmap(bitmap).build();

    final SparseArray<Face> detectedFaces = faceDetector.detect(frame);
    for (int i = 0; i < detectedFaces.size(); i++) {
      whichEmoji(detectedFaces.get(i));
    }

    Log.d("TEST", "detected faces: " + detectedFaces.size());
    if (detectedFaces.size() == 0) {
      Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();
    }

    faceDetector.release();
  }

  private static Emoji whichEmoji(final Face face) {
    final float isLeftEyeOpenProbability = face.getIsLeftEyeOpenProbability();
    final float isRightEyeOpenProbability = face.getIsRightEyeOpenProbability();
    final float isSmilingProbability = face.getIsSmilingProbability();

    final boolean smiling = isSmilingProbability > SMILING_PROB_THRESHOLD;
    final boolean leftEyeClosed = isLeftEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD;
    final boolean rightEyeClosed = isRightEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD;

    if (smiling) {
      if (leftEyeClosed && !rightEyeClosed) {
        return Emoji.LEFT_WINK;
      } else if (rightEyeClosed && !leftEyeClosed) {
        return Emoji.RIGHT_WINK;
      } else if (leftEyeClosed) {
        return Emoji.CLOSED_EYE_SMILE;
      } else {
        return Emoji.SMILE;
      }
    } else {
      if (leftEyeClosed && !rightEyeClosed) {
        return Emoji.LEFT_WINK_FROWN;
      } else if (rightEyeClosed && !leftEyeClosed) {
        return Emoji.RIGHT_WINK_FROWN;
      } else if (leftEyeClosed) {
        return Emoji.CLOSED_EYE_FROWN;
      } else {
        return Emoji.FROWN;
      }
    }
  }
}
