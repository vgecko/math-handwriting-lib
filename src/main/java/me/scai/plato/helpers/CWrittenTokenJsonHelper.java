/**
 * Created by scai on 3/25/2015.
 */

package me.scai.plato.helpers;

import com.google.gson.*;
import me.scai.handwriting.*;

public class CWrittenTokenJsonHelper {
    private static final Gson gson = new Gson();

    /**
     * Serialize CWrittenToken
     * @param wt
     * @return
     */
    public static JsonObject CWrittenToken2JsonObject(CWrittenToken wt) {
        JsonObject wtObj = new JsonObject();

        /* numStrokes */
        final int numStrokes = wt.nStrokes();
        wtObj.add("numStrokes", new JsonPrimitive(numStrokes));

        /* strokes */
        JsonObject strokes = new JsonObject();
        for (int i = 0; i < numStrokes; ++i) {
            JsonObject stroke = CStrokeJsonHelper.CStroke2JsonObject(wt.getStroke(i));

            strokes.add(Integer.toString(i), stroke);
        }

        wtObj.add("strokes", strokes);
        wtObj.add("bNormalized", new JsonPrimitive(wt.bNormalized));

        return wtObj;
    }

    /**
     * Serialize NodeToken
     * @param token
     * @return
     */
    public static JsonObject CWrittenToken2JsonObject(NodeToken token) {
        JsonObject wtObj = new JsonObject();

        /* Node */
        wtObj.add("node", gson.toJsonTree(token.getNode()));

        /* Written token set */
        CAbstractWrittenTokenSet wtSet = token.getTokenSet();
        assert(wtSet instanceof CWrittenTokenSetNoStroke);

        wtObj.add("wtSet", CWrittenTokenSetJsonHelper.CWrittenTokenSetNoStroke2JsonObj((CWrittenTokenSetNoStroke) wtSet)); // TODO
        wtObj.add("matchingGraphicalProductionIndices", gson.toJsonTree(token.getMatchingGraphicalProductionIndices()));

        return wtObj;
    }

    public static String CWrittenToken2JsonNoStroke(CWrittenToken wt) {
        return gson.toJson(CWrittenToken2JsonObjNoStroke(wt));
    }

    public static JsonObject CWrittenToken2JsonObjNoStroke(CWrittenToken wt) {
        if ( !wt.bNormalized ) {
            throw new RuntimeException("Attempt to generate JSON string from un-normalized CWrittenToken object");
        }

        JsonObject obj = new JsonObject();

        /* Get the bounds: [min_x, min_y, max_x, max_y] */
        float [] bounds = wt.getBounds();
        JsonArray jsonBounds = new JsonArray();
        for (int i = 0; i < bounds.length; ++i) {
            jsonBounds.add(new JsonPrimitive(bounds[i]));
        }

        obj.add("bounds", jsonBounds);

        /* Width and height */
        obj.add("width", new JsonPrimitive(wt.width));
        obj.add("height", new JsonPrimitive(wt.height));

        /* Get the recognition winner (if exists) */
        if (wt.getRecogResult() != null) {
            obj.add("recogWinner", new JsonPrimitive(wt.getRecogResult()));
        }

        /* Get the recognition p-values (if exists) */
        if (wt.getRecogPs() != null) {
            double [] recogPs = wt.getRecogPs();
            JsonArray jsonRecogPs = new JsonArray();

            for (int i = 0; i < recogPs.length; ++i) {
                jsonRecogPs.add(new JsonPrimitive(recogPs[i]));
            }

            obj.add("recogPs", jsonRecogPs);
        }

        return obj;
    }
    
    public static CWrittenToken jsonObj2CWrittenTokenNoStroke(JsonObject jsonObj) {
        CWrittenToken wt = new CWrittenToken();
        
        /* Width and height */
        if (jsonObj.has("width")) {
            wt.width = jsonObj.get("width").getAsFloat();
        }
        if (jsonObj.has("height")) {
            wt.height = jsonObj.get("height").getAsFloat();
        }
        
        /* Bounds */
        if (jsonObj.has("bounds")) {
            JsonArray jsBounds = jsonObj.get("bounds").getAsJsonArray();
            
            float [] bounds = new float[jsBounds.size()];
            for (int i = 0; i < jsBounds.size(); ++i) {
                bounds[i] = jsBounds.get(i).getAsFloat();
            }
            
            wt.setBounds(bounds);
        }
        
        /* Recognition winner */
        if (jsonObj.has("recogWinner")) {
            wt.setRecogResult(jsonObj.get("recogWinner").getAsString());
        }
        
        /* Recognition p-values */
        if (jsonObj.has("recogPs")) {            
            JsonArray ps = jsonObj.get("recogPs").getAsJsonArray();
            double [] recogPs = new double[ps.size()];
            
            for (int i = 0; i < ps.size(); ++i) {
                recogPs[i] = ps.get(i).getAsDouble();
            }
            wt.setRecogPs(recogPs);
        }

//        wt.bNormalized = jsonObj.get("bNormalized").getAsBoolean();
        wt.normalizeAxes();
        
        return wt;
    }
}
