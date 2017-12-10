package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

  private static final double SMILING_PROB_THRESHOLD = .15;
  private static final double EYE_OPEN_PROB_THRESHOLD = .5;
  private static final float EMOJI_SCALE_FACTOR = .9f;

  public static Bitmap detectFacesAndOverlayEmoji(final Context context, final Bitmap bitmap) {
    final FaceDetector faceDetector = new FaceDetector.Builder(context)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setTrackingEnabled(false)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build();

    Bitmap resultBitmap = bitmap;
    if (faceDetector.isOperational()){

      final Frame frame = new Frame.Builder().setBitmap(bitmap).build();
      final SparseArray<Face> detectedFaces = faceDetector.detect(frame);
      if (detectedFaces.size() == 0) {
        Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();
      } else {
        for (int i = 0; i < detectedFaces.size(); i++) {
          Bitmap emojiBitmap = null;
          switch (whichEmoji(detectedFaces.get(i))) {
            case FROWN:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
              break;
            case LEFT_WINK:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
              break;
            case RIGHT_WINK:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
              break;
            case LEFT_WINK_FROWN:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
              break;
            case RIGHT_WINK_FROWN:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
              break;
            case CLOSED_EYE_SMILE:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
              break;
            case CLOSED_EYE_FROWN:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
              break;
            case SMILE:
            default:
              emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
              break;
          }
          resultBitmap = addBitmapToFace(bitmap, emojiBitmap, detectedFaces.get(i));
        }
      }
    } else {
      Toast.makeText(context, "Not operational", Toast.LENGTH_SHORT).show();
    }
    faceDetector.release();
    return resultBitmap;
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


  private static Bitmap addBitmapToFace(final Bitmap backgroundBitmap, final Bitmap emojiBitmap, final Face face) {
    // Initialize the results bitmap to be a mutable copy of the original image
    final Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

    // Scale the emoji so it looks better on the face
    float scaleFactor = EMOJI_SCALE_FACTOR;

    // Determine the size of the emoji to match the width of the face and preserve aspect ratio
    final int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
    final int newEmojiHeight = (int) (emojiBitmap.getHeight() * newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);

    // Scale the emoji
    final Bitmap scaledBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

    // Determine the emoji position so it best lines up with the face
    float emojiPositionX = (face.getPosition().x + face.getWidth() / 2) - scaledBitmap.getWidth() / 2;
    float emojiPositionY = (face.getPosition().y + face.getHeight() / 2) - scaledBitmap.getHeight() / 3;

    // Create the canvas and draw the bitmaps to it
    final Canvas canvas = new Canvas(resultBitmap);
    canvas.drawBitmap(backgroundBitmap, 0, 0, null);
    canvas.drawBitmap(scaledBitmap, emojiPositionX, emojiPositionY, null);

    return resultBitmap;
  }
}
