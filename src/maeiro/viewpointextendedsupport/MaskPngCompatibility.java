package maeiro.viewpointextendedsupport;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.ImageIO;

final class MaskPngCompatibility {
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a
    };

    private MaskPngCompatibility() {
    }

    static Object decodeOneBitMask(Path path) {
        if (path == null || !isMaskPng(path) || !hasOneBitDepth(path)) {
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int bufferSize = Math.multiplyExact(Math.multiplyExact(width, height), 4);
            ByteBuffer rgba = ByteBuffer.allocateDirect(bufferSize);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    rgba.put((byte) (argb >>> 16));
                    rgba.put((byte) (argb >>> 8));
                    rgba.put((byte) argb);
                    rgba.put((byte) (argb >>> 24));
                }
            }
            rgba.flip();

            Class<?> lookImage = Class.forName("viewpoint.render.LookImage", true,
                    MaskPngCompatibility.class.getClassLoader());
            Constructor<?> constructor = lookImage.getDeclaredConstructor(int.class, int.class, ByteBuffer.class);
            constructor.setAccessible(true);
            return constructor.newInstance(width, height, rgba);
        } catch (IOException | ReflectiveOperationException | ArithmeticException exception) {
            return null;
        }
    }

    private static boolean isMaskPng(Path path) {
        String normalized = path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        return normalized.endsWith(".png") && (normalized.contains("/masks/") || normalized.startsWith("masks/"));
    }

    private static boolean hasOneBitDepth(Path path) {
        byte[] header = new byte[26];
        try (InputStream input = Files.newInputStream(path)) {
            int offset = 0;
            while (offset < header.length) {
                int read = input.read(header, offset, header.length - offset);
                if (read < 0) {
                    return false;
                }
                offset += read;
            }
        } catch (IOException exception) {
            return false;
        }

        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (header[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }
        return header[12] == 'I' && header[13] == 'H' && header[14] == 'D' && header[15] == 'R'
                && (header[24] & 0xff) == 1;
    }
}
