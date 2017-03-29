package augmentation;

/**
 * Electronic filter that passes low-frequency signals,
 * but attenuates signals with frequencies higher than a cutoff
 * see:
 *  - http://www.raweng.com/blog/2013/05/28/applying-low-pass-filter-to-android-sensors-readings/
 *  - http://www.kircherelectronics.com/blog/index.php/11-android/sensors/8-low-pass-filter-the-basics
 */
public class LowPassFilter {

    private static final float ALPHA_DEFAULT = 0.1f;
    private static final float ALPHA_STEADY       = 0.0001f;
    private static final float ALPHA_START_MOVING = 0.3f;
    private static final float ALPHA_MOVING       = 0.8f;

    private LowPassFilter() { }

    public static float[] filter(float low, float high, float[] current, float[] previous) {
        if (current==null || previous==null) 
            throw new NullPointerException("Input and prev float arrays must be non-NULL");
        if (current.length!=previous.length) 
            throw new IllegalArgumentException("Input and prev must be the same length");

        float alpha = computeAlpha(low,high,current,previous);
        
        for ( int i=0; i<current.length; i++ ) {
            previous[i] = previous[i] + alpha * (current[i] - previous[i]);
        }
        return previous;
    }
    
    private static final float computeAlpha(float low, float high, float[] current, float[] previous) {
        if(previous.length != 3 || current.length != 3)
            return ALPHA_DEFAULT;
        
        float x1 = current[0],
              y1 = current[1],
              z1 = current[2];

        float x2 = previous[0],
              y2 = previous[1],
              z2 = previous[2];
        
        float distance = (float)(Math.sqrt(Math.pow((double) (x2 - x1), 2d) +
                Math.pow((double) (y2 - y1), 2d) +
                Math.pow((double) (z2 - z1), 2d))
        );
        
        if(distance < low) {
            return ALPHA_STEADY;
        } else if(distance >= low || distance < high) {
            return ALPHA_START_MOVING;
        } 
        return ALPHA_MOVING;
    }
}