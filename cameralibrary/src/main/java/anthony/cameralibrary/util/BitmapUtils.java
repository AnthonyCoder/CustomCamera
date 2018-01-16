package anthony.cameralibrary.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import anthony.cameralibrary.CameraManager;
import anthony.cameralibrary.constant.ESaveDirectionType;


public class BitmapUtils {
    public static final String TAG = "BitmapUtils";


    public static boolean saveBitmap(Bitmap b, String absolutePath) {
        return saveBitmap(b, absolutePath, 100);
    }

    public static boolean saveBitmap(Bitmap b, String absolutePath, Bitmap.CompressFormat format) {
        return saveBitmap(b, absolutePath, 100, format);
    }

    public static boolean saveBitmap(Bitmap b, String absolutePath, int quality) {
        return saveBitmap(b, absolutePath, quality, Bitmap.CompressFormat.JPEG);
    }

    public static boolean saveBitmap(Bitmap b, String absolutePath, int quality, Bitmap.CompressFormat format) {
        String fileName = absolutePath;
        File f = new File(fileName);
        try {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            b.compress(format, quality, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }


    public static Bitmap createBitmap(Bitmap b, float width, float angle) {
        // 计算缩放比例
        if (b != null) {
            if (b.getWidth() != width) {
                float scale = width / b.getWidth();

                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                matrix.postRotate(angle);

                Bitmap bNew = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                        b.getHeight(), matrix, true);
                return bNew;
            } else {
                return b;
            }
        }
        return null;
    }


    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b != null) {
            if (b.length != 0) {
                return BitmapFactory.decodeByteArray(b, 0, b.length);
            }
        }
        return null;
    }


    public static Bitmap scale(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap outBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) (bitmap.getWidth()), (int) (bitmap.getHeight()), matrix, true);
        bitmap.recycle();
        return outBitmap;
    }


    /**
     * 圆角图片
     *
     * @param context
     * @param resId   资源文件
     * @param ratio   圆角占图片的百分比
     * @return
     */

    public static Bitmap getRoundCornerBitmapByRatio(Context context, int resId, float ratio) {
        Bitmap output = BitmapFactory.decodeResource(context.getResources(), resId);
        return getRoundCornerBitmapByRatio(output, ratio);
    }

    /**
     * 圆角图片
     *
     * @param bm
     * @param ratio 圆角占图片的百分比
     * @return
     */
    public static Bitmap getRoundCornerBitmapByRatio(Bitmap bm, float ratio) {
        try {
            if (bm != null) {
                float roundPx = bm.getWidth() * ratio;
                Bitmap output = Bitmap.createBitmap(bm.getWidth(),
                        bm.getHeight(), Config.ARGB_8888);
                if (output == null) {
                    return null;
                }
                Canvas canvas = new Canvas(output);

                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                canvas.drawBitmap(bm, rect, rect, paint);

                return output;
            }
        } catch (Throwable e) {
        }

        return null;
    }

    public static Bitmap createCaptureBitmap(String filepath) {
        int scale = 1;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filepath, options);
            int IMAGE_MAX_SIZE = 800;
            if (options.outWidth > IMAGE_MAX_SIZE
                    || options.outHeight > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math
                        .log(IMAGE_MAX_SIZE
                                / (double) Math.max(options.outHeight,
                                options.outWidth))
                        / Math.log(0.5)));
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = scale;
            return BitmapFactory.decodeFile(filepath, opt);
        } catch (OutOfMemoryError e) {
            Log.e("memory",
                    "createCaptureBitmap out of memory");
            scale = scale * 2;
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = scale;
                return BitmapFactory.decodeFile(filepath, opt);
            } catch (OutOfMemoryError oe) {
                Log.e("memory",
                        "createCaptureBitmap out of memory second");
                return null;
            }
        }
    }


    /**
     * 处理三星手机拍出来的图片会旋转的问题，同时刷新相册
     *
     * @param imgPath
     */
    public static void checkImageOrientation(String imgPath) {
        ByteArrayOutputStream baos = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        int rotateDegress = 0;
        try {
            ExifInterface exif = new ExifInterface(imgPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotateDegress = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotateDegress = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotateDegress = 270;
            } else {
                return;
            }

            Options opt = new Options();
            opt.inJustDecodeBounds = true;
            Bitmap bit = BitmapFactory.decodeFile(imgPath, opt);
            int width = opt.outWidth;
            int height = opt.outHeight;
            if (width > height && width > 1024) {
                opt.inSampleSize = width / 1024 + 1;
            } else if (height > 1024) {
                opt.inSampleSize = height / 1024 + 1;
            }
            opt.inJustDecodeBounds = false;
            bit = BitmapFactory.decodeFile(imgPath, opt).copy(Config.ARGB_8888, true);

            Matrix matrix = new Matrix();
            matrix.postScale(1.0f, 1.0f);
            matrix.postRotate(rotateDegress);
            Bitmap bitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
                    bit.getHeight(), matrix, false);
            bit.recycle();
            bit = null;
            bit = bitmap;

            baos = new ByteArrayOutputStream();
            bit.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //			String newImagePath = imgPath + "1.jpg";
            File file = new File(imgPath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(baos.toByteArray());
            bos.flush();

            bit.recycle();
            bit = null;

            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 旋转图片
     *
     * @param imgPath
     * @param rotateDegress：旋转角度，顺时针
     * @return 旋转后的图片路径
     */
    public static String rotateImage(String imgPath, int rotateDegress) {
        ByteArrayOutputStream baos = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        if (rotateDegress % 360 == 0
                || rotateDegress % 90 != 0
                ) {
            return imgPath;
        }

        if (TextUtils.isEmpty(imgPath)) {
            return imgPath;
        }

        int index = imgPath.lastIndexOf(".");
        if (index <= 0) {
            index = imgPath.length();
        }
        String outImgPath = imgPath.substring(0, index) + "_" + rotateDegress + ".jpg";
        try {

            Options opt = new Options();
            opt.inJustDecodeBounds = true;

            Bitmap bit = BitmapFactory.decodeFile(imgPath, opt);
            int width = opt.outWidth;
            int height = opt.outHeight;
            if (width > height && width > 1024) {
                opt.inSampleSize = width / 1024 + 1;
            } else if (height > 1024) {
                opt.inSampleSize = height / 1024 + 1;
            }
            opt.inJustDecodeBounds = false;
            bit = BitmapFactory.decodeFile(imgPath, opt).copy(Config.ARGB_8888, true);

            Matrix matrix = new Matrix();
            matrix.postScale(1.0f, 1.0f);
            matrix.postRotate(rotateDegress);
            Bitmap bitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
                    bit.getHeight(), matrix, false);
            bit.recycle();
            bit = null;
            bit = bitmap;

            baos = new ByteArrayOutputStream();
            bit.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //			String newImagePath = imgPath + "1.jpg";
            File file = new File(outImgPath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(baos.toByteArray());
            bos.flush();

            bit.recycle();
            bit = null;

            return outImgPath;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return imgPath;
    }

    public static Bitmap rotateImage(Bitmap bm, int rotateDegress) {
        Matrix m = new Matrix();
        m.setRotate(rotateDegress, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (rotateDegress == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }

    /**
     * 从中心点   按正方形裁切图片  边长为小边
     */
    public static Bitmap cropImage(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int centerX = w / 2;
        int centerY = h / 2;

        //下面这句是关键
        Bitmap outBitmap = Bitmap.createBitmap(bitmap, centerX - wh / 2, centerY - wh / 2, wh, wh, null, false);
        bitmap.recycle();
        return outBitmap;
    }

    /**
     * 按指定区域解码
     *
     * @param is
     * @param rect
     * @param options
     */
    public static Bitmap decode(InputStream is, Rect rect, Options options) throws Exception {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > 9) {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            bitmap = decoder.decodeRegion(rect, options);
        } else {
            Bitmap temp = BitmapFactory.decodeStream(is, null, options);
            bitmap = Bitmap.createBitmap(temp, rect.left, rect.top, rect.width(), rect.height());
            if (temp != null && !temp.isRecycled()) {
                temp.recycle();
            }
        }

        return bitmap;
    }

    public static boolean saveTakePicFile(byte[] data, File saveFile, CameraManager.CameraDirection cameraDirection, ESaveDirectionType eSaveDirectionType) {
        Bitmap bmptemp = BitmapFactory.decodeByteArray(data, 0, data.length);
        //对图片进行旋转处理
        Matrix matrix = new Matrix();
        matrix.reset();
        int degrees = (cameraDirection == CameraManager.CameraDirection.CAMERA_BACK ? 90 : 270);
        matrix.postRotate(degrees);
        if(eSaveDirectionType == ESaveDirectionType.MIRROR){
            matrix.postScale(-1, 1);   //镜像水平翻转
        }

        Bitmap bMapRotate = Bitmap.createBitmap(bmptemp, 0, 0, bmptemp.getWidth(), bmptemp.getHeight(), matrix, true);

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveFile));
            bMapRotate.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
            bos.flush();//输出
            bos.close();//关闭
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
