package com.ticxo.modelengine.api.generator.assets;

import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaItemModel {
    private static final float DIST_DIVIDER = 0.041666668f;
    private static final Map<String, Map<String, int[]>> DISPLAY = new HashMap<String, Map<String, int[]>>(){};
    private final Map<String, String> textures = new HashMap<String, String>();
    private final List<JavaElement> elements = new ArrayList<JavaElement>();
    private transient String name;
    private transient float maxDistToOrigin = 0.0f;
    private Map<String, Map<String, int[]>> display = DISPLAY;

    public void addElement(JavaElement element) {
        this.elements.add(element);
        for (int i = 0; i < 3; ++i) {
            this.maxDistToOrigin = Math.max(Math.max(Math.abs(element.from[i] - 8.0f), Math.abs(element.to[i] - 8.0f)), this.maxDistToOrigin);
        }
    }

    public int scaleToFit() {
        if (this.maxDistToOrigin <= 24.0f) {
            return 1;
        }
        int size = (int)Math.ceil(this.maxDistToOrigin * 0.041666668f);
        float scale = 1.0f / (float)size;
        for (JavaElement element : this.elements) {
            float[] origin = element.getRotation() == null ? null : element.getRotation().origin;
            for (int i = 0; i < 3; ++i) {
                element.from[i] = TMath.clamp((element.from[i] - 8.0f) * scale + 8.0f, -16.0f, 32.0f);
                element.to[i] = TMath.clamp((element.to[i] - 8.0f) * scale + 8.0f, -16.0f, 32.0f);
                if (origin == null) continue;
                origin[i] = (origin[i] - 8.0f) * scale + 8.0f;
            }
        }
        return size;
    }

    public void finalizeModel() {
    }

    public Map<String, String> getTextures() {
        return this.textures;
    }

    public List<JavaElement> getElements() {
        return this.elements;
    }

    public String getName() {
        return this.name;
    }

    public float getMaxDistToOrigin() {
        return this.maxDistToOrigin;
    }

    public Map<String, Map<String, int[]>> getDisplay() {
        return this.display;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxDistToOrigin(float maxDistToOrigin) {
        this.maxDistToOrigin = maxDistToOrigin;
    }

    public void setDisplay(Map<String, Map<String, int[]>> display) {
        this.display = display;
    }

    public static class JavaElement {
        public void from(float[] fArray, float[] fArray2, float f) {
        }

        public void to(float[] fArray, float[] fArray2, float f) {
        }

        public float[] getFrom() {
            return null;
        }

        public float[] getTo() {
            return null;
        }

        public Map<String, Face> getFaces() {
            return null;
        }

        public Rotation getRotation() {
            return null;
        }

        public void setRotation(Rotation rotation) {
        }

        public static class Rotation {
            public void origin(float[] fArray, float[] fArray2) {
            }

            public float[] getOrigin() {
                return null;
            }

            public float getAngle() {
                return 0.0f;
            }

            public String getAxis() {
                return null;
            }

            public void setAngle(float f) {
            }

            public void setAxis(String string) {
            }
        }

        public static class Face {
            public void uv(int n, int n2, float[] fArray) {
            }

            public float[] getUv() {
                return null;
            }

            public int getTintindex() {
                return 0;
            }

            public int getRotation() {
                return 0;
            }

            public String getTexture() {
                return null;
            }

            public void setRotation(int n) {
            }

            public void setTexture(String string) {
            }
        }
    }
}
