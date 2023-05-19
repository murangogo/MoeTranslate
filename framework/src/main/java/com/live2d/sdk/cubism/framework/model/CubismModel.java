/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.live2d.sdk.cubism.framework.model;

import com.live2d.sdk.cubism.core.*;
import com.live2d.sdk.cubism.framework.CubismFramework;
import com.live2d.sdk.cubism.framework.id.CubismId;
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer;
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer.CubismBlendMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag.*;
import static com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.*;


/**
 * Model class created from Mclapoc data.
 */
public class CubismModel {
    /**
     * Inner class for handling texture colors in RGBA
     */
    public static class DrawableColorData {
        /**
         * Default constructor
         */
        public DrawableColorData() {}

        /**
         * Constructor
         *
         * @param isOverwritten flag whether to be overwritten
         * @param color texture color
         * @throws IllegalArgumentException if an argument is null
         */
        public DrawableColorData(boolean isOverwritten, CubismRenderer.CubismTextureColor color) {
            if (color == null) {
                throw new IllegalArgumentException("color is null");
            }
            this.isOverwritten = isOverwritten;
            this.color = color;
        }

        /**
         * Copy constructor
         *
         * @param data DrawableColorData instance to be copied
         */
        public DrawableColorData(DrawableColorData data) {
            this.isOverwritten = data.isOverwritten;
            this.color = data.color;
        }

        /**
         * flag whether to be overwritten
         */
        public boolean isOverwritten;
        /**
         * texture color
         */
        public CubismRenderer.CubismTextureColor color;
    }

    /**
     * テクスチャのカリング設定を管理するための内部クラス
     */
    public static class DrawableCullingData {
        /**
         * コンストラクタ
         */
        DrawableCullingData() {}

        /**
         * コンストラクタ
         *
         * @param isOverWritten モデルのカリング設定を上書きするかどうか
         * @param isCulling カリングするかどうか
         */
        DrawableCullingData(boolean isOverWritten, boolean isCulling) {
            this.isOverWritten = isOverWritten;
            this.isCulling = isCulling;
        }

        /**
         * モデルのカリング設定を上書きするかどうか
         */
        public boolean isOverWritten;
        /**
         * カリングするかどうか
         */
        public boolean isCulling;
    }

    /**
     * Update model's parameters.
     */
    public void update() {
        model.update();
        model.resetDrawableDynamicFlags();
    }

    /**
     * Get the canvas width in Pixel.
     *
     * @return canvas width(pixel)
     */
    public float getCanvasWidthPixel() {
        if (model == null) {
            return 0.0f;
        }
        CubismCanvasInfo canvasInfo = model.getCanvasInfo();

        return canvasInfo.getSizeInPixels()[0];
    }

    /**
     * Get the canvas height in Pixel.
     *
     * @return canvas height(pixel)
     */
    public float getCanvasHeightPixel() {
        if (model == null) {
            return 0.0f;
        }
        CubismCanvasInfo canvasInfo = model.getCanvasInfo();

        return canvasInfo.getSizeInPixels()[1];
    }

    /**
     * Get PixelsPerUnit.
     *
     * @return PixelsPerUnit
     */
    public float getPixelPerUnit() {
        if (model == null) {
            return 0.0f;
        }
        CubismCanvasInfo canvasInfo = model.getCanvasInfo();

        return canvasInfo.getPixelsPerUnit();
    }

    /**
     * Get the canvas width.
     *
     * @return canvas width
     */
    public float getCanvasWidth() {
        if (model == null) {
            return 0.0f;
        }

        CubismCanvasInfo canvasInfo = model.getCanvasInfo();

        return canvasInfo.getSizeInPixels()[0] / canvasInfo.getPixelsPerUnit();
    }

    /**
     * Get the canvas height.
     *
     * @return canvas height
     */
    public float getCanvasHeight() {
        if (model == null) {
            return 0.0f;
        }

        CubismCanvasInfo canvasInfo = model.getCanvasInfo();

        return canvasInfo.getSizeInPixels()[1] / canvasInfo.getPixelsPerUnit();
    }

    /**
     * Get the index of parts.
     *
     * @param partId parts ID
     * @return parts index
     */
    public int getPartIndex(CubismId partId) {
        final CubismPartView partView = model.findPartView(partId.getString());
        if (partView != null) {
            return partView.getIndex();
        }

        // If the part does not exist in the model, it searches for it in the non-existent part ID list and returns its index.
        if (notExistPartIds.containsKey(partId)) {
            return notExistPartIds.get(partId);
        }

        // If the part does not exist in the non-existent part ID list, add newly the element.
        final int partCount = partValues.length;
        final int partIndex = partCount + notExistPartIds.size();
        notExistPartIds.put(partId, partIndex);
        notExistPartIndices.add(partIndex);

        float[] tmp = new float[notExistPartIndices.size()];
        System.arraycopy(notExistPartOpacities, 0, tmp, 0, notExistPartIndices.size() - 1);
        tmp[notExistPartIndices.size() - 1] = 0.0f;
        notExistPartOpacities = new float[notExistPartIndices.size()];
        System.arraycopy(tmp, 0, notExistPartOpacities, 0, notExistPartIndices.size());

        return partIndex;
    }

    /**
     * Get the number of parts.
     *
     * @return number of parts
     */
    public int getPartCount() {
        return model.getPartViews().length;
    }

    /**
     * Set an opacity of the part.
     *
     * @param partId part ID
     * @param opacity opacity
     */
    public void setPartOpacity(CubismId partId, float opacity) {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        final int index = getPartIndex(partId);

        if (index < 0) {
            // Skip processes because there is no part.
            return;
        }

        setPartOpacity(index, opacity);
    }

    /**
     * Set an opacity of the part.
     *
     * @param partIndex part index
     * @param opacity part opacity
     */
    public void setPartOpacity(int partIndex, float opacity) {
        if (notExistPartIndices.contains(partIndex)) {
            int index = notExistPartIndices.indexOf(partIndex);
            notExistPartOpacities[index] = opacity;
            return;
        }

        // Detect whether partIndex is not out of bounds index
        assert 0 <= partIndex && partIndex < getPartCount();

        partValues[partIndex].setOpacity(opacity);
    }

    /**
     * Get the opacity of the part.
     *
     * @param partId part ID
     * @return part opacity
     */
    public float getPartOpacity(CubismId partId) {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        final int index = getPartIndex(partId);

        if (index < 0) {
            // Skip processes because there is no part
            return 0;
        }

        return getPartOpacity(index);
    }

    /**
     * Get the opacity of the part.
     *
     * @param partIndex part index
     * @return part opacity
     */
    public float getPartOpacity(int partIndex) {
        if (notExistPartIndices.contains(partIndex)) {
            // If the part ID does not exist in the model, returns the opacity from non-existence parts list.
            int index = notExistPartIndices.indexOf(partIndex);
            return notExistPartOpacities[index];
        }

        // Detect whether partIndex is not out of bounds index
        assert 0 <= partIndex && partIndex < getPartCount();

        return partValues[partIndex].getOpacity();
    }

    /**
     * Get the index of parameters.
     *
     * @param parameterId parameter ID
     * @return parameter index
     */
    public int getParameterIndex(CubismId parameterId) {
        final CubismParameterView parameterView = model.findParameterView(parameterId.getString());
        if (parameterView != null) {
            return parameterView.getIndex();
        }

        // If the parameter does not exist in the model, it searches for it in the non-existent parameter ID list and returns its index.
        if (notExistParameterIds.containsKey(parameterId)) {
            final Integer index = notExistParameterIds.get(parameterId);
            assert index != null;
            return index;
        }

        // If the parameter does not exist in the non-existent parameter ID list, add newly the element.
        final int parameterCount = parameterValues.length;
        final int parameterIndex = parameterCount + notExistParameterIds.size();
        notExistParameterIds.put(parameterId, parameterIndex);
        notExistParameterIndices.add(parameterIndex);

        float[] tmp = new float[notExistParameterIndices.size()];
        System.arraycopy(notExistParameterValues, 0, tmp, 0, notExistParameterIndices.size() - 1);
        tmp[notExistParameterIndices.size() - 1] = 0.0f;
        notExistParameterValues = new float[notExistParameterIndices.size()];
        System.arraycopy(tmp, 0, notExistParameterValues, 0, notExistParameterIndices.size());

        return parameterIndex;
    }

    /**
     * Get the number of parameters.
     *
     * @return the number of parameters
     */
    public int getParameterCount() {
        return parameterValues.length;
    }

    /**
     * Return the type of parameter at the index specified by the argument.
     *
     * @param parameterIndex parameter index
     * @return the type of parameter at the index specified by the argument
     */
    public CubismParameters.ParameterType getParameterType(int parameterIndex) {
        return model.getParameterViews()[parameterIndex].getType();
    }

    /**
     * Get the maximum value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the maximum value of parameter
     */
    public float getParameterMaximumValue(int parameterIndex) {
        return model.getParameterViews()[parameterIndex].getMaximumValue();
    }

    /**
     * Get the minimum value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the minimum value of parameter
     */
    public float getParameterMinimumValue(int parameterIndex) {
        return model.getParameterViews()[parameterIndex].getMinimumValue();
    }

    /**
     * Get the default value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the default value of parameter
     */
    public float getParameterDefaultValue(int parameterIndex) {
        return model.getParameterViews()[parameterIndex].getDefaultValue();
    }

    /**
     * Get the value of parameter.
     *
     * @param parameterId parameter ID
     * @return parameter value
     */
    public float getParameterValue(CubismId parameterId) {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        final int parameterIndex = getParameterIndex(parameterId);
        return getParameterValue(parameterIndex);
    }

    /**
     * Get the value of parameter.
     *
     * @param parameterIndex parameter index
     * @return the value of parameter
     */
    public float getParameterValue(int parameterIndex) {
        if (notExistParameterIndices.contains(parameterIndex)) {
            int index = notExistParameterIndices.indexOf(parameterIndex);
            final float value = notExistParameterValues[index];
            return value;
        }

        // Detect whether partIndex is not out of bounds index
        assert 0 <= parameterIndex && parameterIndex < getParameterCount();

        return parameterValues[parameterIndex].getValue();
    }

    /**
     * Set the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value parameter value
     */
    public void setParameterValue(CubismId parameterId, float value) {
        setParameterValue(parameterId, value, 1.0f);
    }

    /**
     * Set the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value parameter value
     * @param weight weight
     */
    public void setParameterValue(CubismId parameterId, float value, float weight) {
        final int index = getParameterIndex(parameterId);
        setParameterValue(index, value, weight);
    }

    /**
     * Set the value of parameter.
     *
     * @param parameterIndex parametere index
     * @param value parameter value
     */
    public void setParameterValue(int parameterIndex, float value) {
        setParameterValue(parameterIndex, value, 1.0f);
    }

    /**
     * Set the value of parameter.
     *
     * @param parameterIndex parameter index
     * @param value parameter value
     * @param weight weight
     */
    public void setParameterValue(int parameterIndex, float value, float weight) {
        if (notExistParameterIndices.contains(parameterIndex)) {
            int index = notExistParameterIndices.indexOf(parameterIndex);
            final float parameterValue = notExistParameterValues[index];
            final float weightedParameterValue = (weight == 1.0f)
                                                 ? value
                                                 : (parameterValue * (1.0f - weight)) + (value * weight);
            notExistParameterValues[index] = weightedParameterValue;
            return;
        }


        // Detect whether partIndex is not out of bounds index
        assert 0 <= parameterIndex && parameterIndex < getParameterCount();

        CubismParameterView parameter = parameterValues[parameterIndex];
        if (parameter.getMaximumValue() < value) {
            value = parameter.getMaximumValue();
        } else if (parameter.getMinimumValue() > value) {
            value = parameter.getMinimumValue();
        }

        final float parameterValue = parameter.getValue();
        final float weightedParameterValue = (weight == 1.0f)
                                             ? value
                                             : (parameterValue * (1.0f - weight)) + (value * weight);
        parameter.setValue(weightedParameterValue);
    }

    /**
     * Add the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value the value to be added
     */
    public void addParameterValue(CubismId parameterId, float value) {
        addParameterValue(parameterId, value, 1.0f);
    }

    /**
     * Add the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value the value to be added
     * @param weight weight
     */
    public void addParameterValue(CubismId parameterId, float value, float weight) {
        final int index = getParameterIndex(parameterId);
        addParameterValue(index, value, weight);
    }

    /**
     * Add the value of parameter.
     *
     * @param parameterIndex parameter index
     * @param value the value to be added
     */
    public void addParameterValue(int parameterIndex, float value) {
        addParameterValue(parameterIndex, value, 1.0f);
    }

    /**
     * Add the value of parameter.
     *
     * @param parameterIndex parameter index
     * @param value the value to be added
     * @param weight weight
     */
    public void addParameterValue(int parameterIndex, float value, float weight) {
        setParameterValue(
            parameterIndex,
            getParameterValue(parameterIndex) + (value * weight)
        );
    }

    /**
     * Multiply the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value the value to be multiplied
     */
    public void multiplyParameterValue(CubismId parameterId, float value) {
        multiplyParameterValue(parameterId, value, 1.0f);
    }

    /**
     * Multiply the value of parameter.
     *
     * @param parameterId parameter ID
     * @param value the value to be multiplied
     * @param weight weight
     */
    public void multiplyParameterValue(CubismId parameterId, float value, float weight) {
        final int index = getParameterIndex(parameterId);
        multiplyParameterValue(index, value, weight);
    }

    /**
     * Multiply the value of parameter.
     *
     * @param parameterIndex parameter index
     * @param value the value to be multiplied
     */
    public void multiplyParameterValue(int parameterIndex, float value) {
        multiplyParameterValue(parameterIndex, value, 1.0f);
    }

    /**
     * Multiply the value of parameter.
     *
     * @param parameterIndex parameter index
     * @param value the value to be multiplied
     * @param weight weight
     */
    public void multiplyParameterValue(int parameterIndex, float value, float weight) {
        setParameterValue(
            parameterIndex,
            getParameterValue(parameterIndex) * (1.0f + (value - 1.0f) * weight)
        );
    }

    /**
     * Get the index of Drawable.
     *
     * @param drawableId Drawable ID
     * @return Drawable index. If there is no index, return -1.
     */
    public int getDrawableIndex(CubismId drawableId) {
        final CubismDrawableView drawableIndex = model.findDrawableView(drawableId.getString());
        if (drawableIndex != null) {
            return drawableIndex.getIndex();
        }

        return -1;
    }

    /**
     * Get the number of Drawable.
     *
     * @return the number of Drawable
     */
    public int getDrawableCount() {
        return model.getDrawableViews().length;
    }

    /**
     * Get Drawable ID.
     *
     * @param drawableIndex Drawable index
     * @return Drawable ID
     */
    public CubismId getDrawableId(int drawableIndex) {
        final String drawableId = model.getDrawableViews()[drawableIndex].getId();
        return CubismFramework.getIdManager().getId(drawableId);
    }

    /**
     * Get the drawing order list of Drawable.
     *
     * @return the drawing order list of Drawable
     */
    public int[] getDrawableRenderOrders() {
        final CubismDrawableView[] drawableViews = model.getDrawableViews();
        assert drawableViews != null;

        if (drawableViews.length > 0) {
            return drawableViews[0].getDrawables().getRenderOrders();
        } else {
            return new int[0];
        }
    }

    /**
     * Get the texture index of Drawable
     *
     * @param drawableIndex Drawable index
     * @return the texture index of Drawable
     */
    public int getDrawableTextureIndex(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getTextureIndex();
    }

    /**
     * Get the vertex index list of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the vertex index list of Drawable
     */
    public short[] getDrawableVertexIndices(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getIndices();
    }

    /**
     * Get the number of the vertex indices in Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the number of vertex indices in Drawable
     */
    public int getDrawableVertexIndexCount(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getIndices().length;
    }

    /**
     * Get the number of the vertex in Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the number of vertex in Drawable
     */
    public int getDrawableVertexCount(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getVertexCount();
    }

    /**
     * Get the vertex list of Drawable
     *
     * @param drawableIndex Drawable index
     * @return the vertex list of Drawable
     */
    public float[] getDrawableVertices(int drawableIndex) {
        return getDrawableVertexPositions(drawableIndex);
    }

    /**
     * Get the vertex positions list of Drawable
     *
     * @param drawableIndex Drawable index
     * @return the vertex positions list of Drawable
     */
    public float[] getDrawableVertexPositions(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getVertexPositions();
    }

    /**
     * Get the vertex UV list of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the vertex UV list of Drawable
     */
    public float[] getDrawableVertexUvs(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getVertexUvs();
    }

    /**
     * Get the opacity of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the Drawable opacity
     */
    public float getDrawableOpacity(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getOpacity();
    }

    /**
     * Get the multiply color of the drawable.
     *
     * @param drawableIndex index of the drawable
     * @return multiply color of the drawable
     */
    public float[] getDrawableMultiplyColor(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getMultiplyColors();
    }

    /**
     * Get the screen color of the drawable.
     *
     * @param drawableIndex index of the drawable
     * @return screen color of the drawable
     */
    public float[] getDrawableScreenColor(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getScreenColors();
    }

    /**
     * Return the index of the Drawable's parent part.
     *
     * @param drawableIndex the drawable index
     * @return the index of the Drawable's parent part
     */
    public int getDrawableParentPartIndex(int drawableIndex) {
        return model.getDrawableViews()[drawableIndex].getParentPartIndex();
    }

    /**
     * Get the blend mode of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the blend mode of Drawable
     */
    public CubismBlendMode getDrawableBlendMode(int drawableIndex) {
        final byte constantFlag = model.getDrawableViews()[drawableIndex].getConstantFlag();
        return isBitSet(constantFlag, BLEND_ADDITIVE)
               ? CubismBlendMode.ADDITIVE
               : isBitSet(constantFlag, BLEND_MULTIPLICATIVE)
                 ? CubismBlendMode.MULTIPLICATIVE
                 : CubismBlendMode.NORMAL;
    }

    /**
     * Get Drawable's invert setting when mask is used.
     * If mask is not used, nothing happens.
     *
     * @param drawableIndex Drawable index
     * @return the invert setting of Drawable's mask
     */
    public boolean getDrawableInvertedMask(int drawableIndex) {
        final byte constantFlag = model.getDrawableViews()[drawableIndex].getConstantFlag();

        return isBitSet(constantFlag, IS_INVERTED_MASK);
    }

    /**
     * Get the visible information of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return Drawable is visible, return true
     */
    public boolean getDrawableDynamicFlagIsVisible(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, IS_VISIBLE);
    }

    /**
     * In recent {@link CubismModel#update()}, if the visible state is changed, return true.
     *
     * @param drawableIndex Drawable index
     * @return If the visible state is changed, return true
     */
    public boolean getDrawableDynamicFlagVisibilityDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, VISIBILITY_DID_CHANGE);
    }

    /**
     * In recent {@link CubismModel#update()}, if the opacity of drawable is changed, return true.
     *
     * @param drawableIndex Drawable index
     * @return If the opacity of drawable is changed, return true
     */
    public boolean getDrawableDynamicFlagOpacityDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, OPACITY_DID_CHANGE);
    }

    /**
     * In recent {@link CubismModel#update()}, if DrawOrder of Drawable is changed, return true.
     * DrawOrder is the information from 0 to 1000 specified in ArtMesh.
     *
     * @param drawableIndex Drawable index
     * @return If DrawOrder of Drawable is changed, return true
     */
    public boolean getDrawableDynamicFlagDrawOrderDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, DRAW_ORDER_DID_CHANGE);
    }

    /**
     * In recent {@link CubismModel#update()}, if the drawing order of Drawable is changed, return true.
     *
     * @param drawableIndex Drawable index
     * @return If the drawing order of Drawable is changed, return true
     */
    public boolean getDrawableDynamicFlagRenderOrderDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, RENDER_ORDER_DID_CHANGE);
    }

    /**
     * In recent {@link CubismModel#update()}, if the vertex information of Drawable is changed, return true.
     *
     * @param drawableIndex Drawable index
     * @return If the vertex information of Drawable is changed, return true
     */
    public boolean getDrawableDynamicFlagVertexPositionsDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, VERTEX_POSITIONS_DID_CHANGE);
    }

    /**
     * Get whether the last CubismModel.update() method changed the Drawable's multiply and screen colors.
     *
     * @param drawableIndex index of the drawable
     * @return Whether the drawable's multiply and screen color is changed in the last CubismModel.update() method
     */
    public boolean getDrawableDynamicFlagBlendColorDidChange(int drawableIndex) {
        final byte dynamicFlag = model.getDrawableViews()[drawableIndex].getDynamicFlag();
        return isBitSet(dynamicFlag, BLEND_COLOR_DID_CHANGE);
    }

    /**
     * Get the clipping mask list of Drawable.
     *
     * @return the clipping mask list of Drawable
     */
    public int[][] getDrawableMasks() {
        final CubismDrawableView[] drawableViews = model.getDrawableViews();
        assert drawableViews != null;

        if (drawableViews.length > 0) {
            return drawableViews[0].getDrawables().getMasks();
        } else {
            return new int[0][0];
        }
    }

    /**
     * Get the number list of the clippng mask in Drawable.
     *
     * @return the number list of the clipping mask in Drawable
     */
    public int[] getDrawableMaskCounts() {
        final CubismDrawableView[] drawableViews = model.getDrawableViews();
        assert drawableViews != null;

        if (drawableViews.length > 0) {
            return drawableViews[0].getDrawables().getMaskCounts();
        } else {
            return new int[0];
        }
    }


    /**
     * Whether clipping mask is used.
     *
     * @return If clipping mask is used, return true.
     */
    public boolean isUsingMasking() {
        final CubismDrawableView[] drawableViews = model.getDrawableViews();
        assert drawableViews != null;

        if (drawableViews.length > 0) {
            final int drawableCount = drawableViews.length;

            for (int i = 0; i < drawableCount; ++i) {
                final int[] drawableMaskCounts = getDrawableMaskCounts();
                if (drawableMaskCounts != null && drawableMaskCounts[i] <= 0) {
                    continue;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Read the saved parameters.
     */
    public void loadParameters() {
        int parameterCount = getParameterCount();
        final int savedParameterCount = savedParameters.length;

        if (parameterCount > savedParameterCount) {
            parameterCount = savedParameterCount;
        }

        for (int i = 0; i < parameterCount; ++i) {
            parameterValues[i].setValue(savedParameters[i]);
        }
    }

    /**
     * Save the parameters.
     */
    public void saveParameters() {
        final int parameterCount = getParameterCount();

        if (savedParameters.length < parameterCount) {
            savedParameters = new float[parameterCount];
        }
        for (int i = 0; i < parameterCount; i++) {
            savedParameters[i] = parameterValues[i].getValue();
        }
    }

    /**
     * Get the multiply color from the list.
     *
     * @param drawableIndex index of the drawable
     * @return the multiply color
     */
    public CubismRenderer.CubismTextureColor getMultiplyColor(int drawableIndex) {
        if (getOverwriteFlagForModelMultiplyColors() || getOverwriteFlagForDrawableMultiplyColors(drawableIndex)) {
            return userMultiplyColors.get(drawableIndex).color;
        }

        float[] color = getDrawableMultiplyColor(drawableIndex);
        multiplyColor.r = color[0];
        multiplyColor.g = color[1];
        multiplyColor.b = color[2];
        multiplyColor.a = color[3];

        return multiplyColor;
    }

    // This is only used by 'getMultiplyColor' method.
    // Avoid creating a new CubismTextureColor instance in getter method.
    private final CubismRenderer.CubismTextureColor multiplyColor = new CubismRenderer.CubismTextureColor();

    /**
     * Get the screen color from the list.
     *
     * @param drawableIndex index of the drawable
     * @return the screen color
     */
    public CubismRenderer.CubismTextureColor getScreenColor(int drawableIndex) {
        if (getOverwriteFlagForModelScreenColors() || getOverwriteFlagForDrawableScreenColors(drawableIndex)) {
            return userScreenColors.get(drawableIndex).color;
        }

        float[] color = getDrawableScreenColor(drawableIndex);
        screenColor.r = color[0];
        screenColor.g = color[1];
        screenColor.b = color[2];
        screenColor.a = color[3];

        return screenColor;
    }

    // This is only used by 'getScreenColor' method.
    // Avoid creating a new CubismTextureColor instance in getter method.
    private final CubismRenderer.CubismTextureColor screenColor = new CubismRenderer.CubismTextureColor();

    /**
     * Set the multiply color.
     *
     * @param drawableIndex index of the drawable
     * @param color the multiply color instance
     */
    public void setMultiplyColor(int drawableIndex, CubismRenderer.CubismTextureColor color) {
        setMultiplyColor(drawableIndex, color.r, color.g, color.b, color.a);
    }

    /**
     * Set the multiply color.
     *
     * @param drawableIndex index of the drawable
     * @param r red value
     * @param g green value
     * @param b blue value
     * @param a alpha value
     */
    public void setMultiplyColor(int drawableIndex, float r, float g, float b, float a) {
        userMultiplyColors.get(drawableIndex).color.r = r;
        userMultiplyColors.get(drawableIndex).color.g = g;
        userMultiplyColors.get(drawableIndex).color.b = b;
        userMultiplyColors.get(drawableIndex).color.a = a;
    }

    /**
     * Set the screen color.
     *
     * @param drawableIndex index of the drawable
     * @param color the screen color instance
     */
    public void setScreenColor(int drawableIndex, CubismRenderer.CubismTextureColor color) {
        setScreenColor(drawableIndex, color.r, color.g, color.b, color.a);
    }

    /**
     * Set the screen color.
     *
     * @param drawableIndex index of the drawable
     * @param r red value
     * @param g green value
     * @param b blue value
     * @param a alpha value
     */
    public void setScreenColor(int drawableIndex, float r, float g, float b, float a) {
        userScreenColors.get(drawableIndex).color.r = r;
        userScreenColors.get(drawableIndex).color.g = g;
        userScreenColors.get(drawableIndex).color.b = b;
        userScreenColors.get(drawableIndex).color.a = a;
    }

    /**
     * Whether to override the entire model multiply color from the SDK.
     *
     * @return If the color information on the SDK is used, return true. If the color information of the model is used, return false.
     */
    public boolean getOverwriteFlagForModelMultiplyColors() {
        return isOverwrittenModelMultiplyColors;
    }

    /**
     * Whether to override the entire model screen color from the SDK.
     *
     * @return If the color information on the SDK is used, return true. If the color information of the model is used, return false.
     */
    public boolean getOverwriteFlagForModelScreenColors() {
        return isOverwrittenModelScreenColors;
    }

    /**
     * Set the flag whether to override the entire model multiply color from the SDK.
     *
     * @param value If the color information on the SDK is used, this value is true. If the color information of the model is used, this is false.
     */
    public void setOverwriteFlagForModelMultiplyColors(boolean value) {
        isOverwrittenModelMultiplyColors = value;
    }

    /**
     * Set the flag whether to override the entire model screen color from the SDK.
     *
     * @param value If the color information on the SDK is used, this value is true. If the color information of the model is used, this is false.
     */
    public void setOverwriteFlagForModelScreenColors(boolean value) {
        isOverwrittenModelScreenColors = value;
    }

    /**
     * Whether to override the drawable multiply color from the SDK.
     *
     * @return If the color information on the SDK is used, return true. If the color information of the model is used, return false.
     */
    public boolean getOverwriteFlagForDrawableMultiplyColors(int drawableIndex) {
        return userMultiplyColors.get(drawableIndex).isOverwritten;
    }

    /**
     * Whether to override the drawable screen color from the SDK.
     *
     * @return If the color information on the SDK is used, return true. If the color information of the model is used, return false.
     */
    public boolean getOverwriteFlagForDrawableScreenColors(int drawableIndex) {
        return userScreenColors.get(drawableIndex).isOverwritten;
    }

    /**
     * Set the flag whether to override the drawable multiply color from the SDK.
     *
     * @param value If the color information on the SDK is used, this value is true. If the color information of the model is used, this is false.
     */
    public void setOverwriteFlagForDrawableMultiplyColors(int drawableIndex, boolean value) {
        userMultiplyColors.get(drawableIndex).isOverwritten = value;
    }

    /**
     * Set the flag whether to override the drawable screen color from the SDK.
     *
     * @param value If the color information on the SDK is used, this value is true. If the color information of the model is used, this is false.
     */
    public void setOverwriteFlagForDrawableScreenColors(int drawableIndex, boolean value) {
        userScreenColors.get(drawableIndex).isOverwritten = value;
    }

    /**
     * Get the culling inforamtion of Drawable.
     *
     * @param drawableIndex Drawable index
     * @return the culling inforamtion of Drawable
     */
    public boolean getDrawableCulling(int drawableIndex) {
        if (getOverwriteFlagForModelCullings() || getOverwriteFlagForDrawableCullings(drawableIndex)) {
            return userCullings.get(drawableIndex).isCulling;
        }

        final byte constantFlag = model.getDrawableViews()[drawableIndex].getConstantFlag();
        return !isBitSet(constantFlag, IS_DOUBLE_SIDED);
    }

    /**
     * Drawableのカリング情報を設定する
     *
     * @param drawableIndex drawableのインデックス
     * @param isCulling カリングするかどうか
     */
    public void setDrawableCulling(int drawableIndex, boolean isCulling) {
        userCullings.get(drawableIndex).isCulling = isCulling;
    }

    /**
     * SDKからモデル全体のカリング設定を上書きするか
     *
     * @return trueならSDK上のカリング設定を使用し、falseならモデルのカリング設定を使用する
     */
    public boolean getOverwriteFlagForModelCullings() {
        return isOverwrittenCullings;
    }

    /**
     * SDK上からモデル全体のカリング設定を上書きするかをセットする
     *
     * @param value SDK上のカリング設定を使うならtrue, モデルのカリング設定を使うならfalse
     */
    public void setOverwriteFlagForModelCullings(boolean value) {
        isOverwrittenCullings = value;
    }

    /**
     * SDKからdrawableのカリング設定を上書きするか
     *
     * @param drawableIndex drawableのインデックス
     * @return trueならSDK上のカリング設定を使用し、falseならモデルのカリング設定を使用する
     */
    public boolean getOverwriteFlagForDrawableCullings(int drawableIndex) {
        return userCullings.get(drawableIndex).isOverWritten;
    }

    /**
     * SDKからdrawableのカリング設定を上書きするかをセットする
     *
     * @param drawableIndex drawableのインデックス
     * @param value SDK上のカリング設定を使うならtrue, モデルのカリング設定を使うならfalse
     */
    public void setOverwriteFlagForDrawableCullings(int drawableIndex, boolean value) {
        userCullings.get(drawableIndex).isOverWritten = value;
    }

    /**
     * Get the model.
     *
     * @return model
     */
    public com.live2d.sdk.cubism.core.CubismModel getModel() {
        return model;
    }

    /**
     * Close the CubismModel instance.
     */
    public void close() {
        if (model != null) {
            model.close();
            model.getMoc().close();
        }
    }

    /**
     * Initialize the model.
     */
    void initialize() {
        assert model != null;

        parameterValues = model.getParameterViews();
        partValues = model.getPartViews();

        // Set parameter IDs to _parameterIds.
        for (CubismParameterView parameterValue : parameterValues) {
            String id = parameterValue.getId();

            parameterIds.add(CubismFramework.getIdManager().getId(id));
        }

        // Set part IDs to _partIds.
        for (CubismPartView partValue : partValues) {
            String id = partValue.getId();

            partIds.add(CubismFramework.getIdManager().getId(id));
        }

        // Set drawable IDs to _drawableIds.
        CubismDrawableView[] drawableValues = model.getDrawableViews();

        // MultiplyColors
        CubismRenderer.CubismTextureColor mutiplyColor = new CubismRenderer.CubismTextureColor(
            1.0f,
            1.0f,
            1.0f,
            1.f
        );
        DrawableColorData userMultiplyColor = new DrawableColorData(false, mutiplyColor);

        // ScreenColors
        CubismRenderer.CubismTextureColor screenColor = new CubismRenderer.CubismTextureColor(
            0.0f,
            0.0f,
            0.0f,
            1.0f
        );
        DrawableColorData userScreenColor = new DrawableColorData(false, screenColor);

        for (CubismDrawableView drawableValue : drawableValues) {
            String id = drawableValue.getId();
            drawableIds.add(CubismFramework.getIdManager().getId(id));
            userMultiplyColors.add(new DrawableColorData(userMultiplyColor));
            userScreenColors.add(new DrawableColorData(userScreenColor));
            userCullings.add(new DrawableCullingData(false, false));
        }
    }

    /**
     * Constructor
     *
     * @param model model instance
     */
    CubismModel(final com.live2d.sdk.cubism.core.CubismModel model) {
        this.model = model;
    }

    /**
     * Return true if the logical product of flag and mask matches the mask.
     *
     * @return Return true if the logical product of flag and mask matches the mask.
     */
    private boolean isBitSet(final byte flag, final byte mask) {
        return (flag & mask) == mask;
    }


    /**
     * List of opacities for non-existent parts
     */
    private float[] notExistPartOpacities = new float[1];
    private final List<Integer> notExistPartIndices = new ArrayList<Integer>();

    /**
     * List of IDs for non-existent parts
     */
    private final Map<CubismId, Integer> notExistPartIds = new HashMap<CubismId, Integer>();
    /**
     * List of values for non-existent parameters
     */
    private float[] notExistParameterValues = new float[1];
    private final List<Integer> notExistParameterIndices = new ArrayList<Integer>();
    /**
     * List of IDs for non-existent parameters
     */
    private final Map<CubismId, Integer> notExistParameterIds = new HashMap<CubismId, Integer>();
    /**
     * Saved parameters
     */
    private float[] savedParameters = new float[1];
    /**
     * model
     */
    private final com.live2d.sdk.cubism.core.CubismModel model;

    private CubismParameterView[] parameterValues;
    private CubismPartView[] partValues;

    private List<CubismId> parameterIds = new ArrayList<CubismId>();
    private List<CubismId> partIds = new ArrayList<CubismId>();
    private List<CubismId> drawableIds = new ArrayList<CubismId>();

    /**
     * List of multiply colors
     */
    private List<DrawableColorData> userMultiplyColors = new ArrayList<DrawableColorData>();
    /**
     * List of screen colors
     */
    private List<DrawableColorData> userScreenColors = new ArrayList<DrawableColorData>();
    /**
     * カリング設定のリスト
     */
    private final List<DrawableCullingData> userCullings = new ArrayList<DrawableCullingData>();
    /**
     * Flag whether to overwrite all the multiply colors
     */
    private boolean isOverwrittenModelMultiplyColors;
    /**
     * Flag whether to overwrite all the screen colors
     */
    private boolean isOverwrittenModelScreenColors;
    /**
     * モデルのカリング設定をすべて上書きするか？
     */
    private boolean isOverwrittenCullings;
}
