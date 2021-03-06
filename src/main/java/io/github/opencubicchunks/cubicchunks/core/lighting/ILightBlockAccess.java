/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.core.lighting;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ILightBlockAccess {

    int getBlockLightOpacity(BlockPos pos);

    int getLightFor(EnumSkyBlock lightType, BlockPos pos);

    /**
     * @return success (if cube is loaded)
     */
    boolean setLightFor(EnumSkyBlock lightType, BlockPos pos, int val);

    /**
     * Faster version of world.getRawLight that works for skylight
     */
    default int computeLightValue(BlockPos pos) {
        if (canSeeSky(pos)) {
            return 15;
        }
        int lightSubtract = getBlockLightOpacity(pos);

        if (lightSubtract < 1) {
            lightSubtract = 1;
        }

        if (lightSubtract >= 15) {
            return 0;
        }
        BlockPos.PooledMutableBlockPos currentPos = BlockPos.PooledMutableBlockPos.retain();
        int maxValue = 0;
        for (EnumFacing enumfacing : EnumFacing.values()) {
            currentPos.setPos(pos).move(enumfacing);
            int currentValue = this.getLightFor(EnumSkyBlock.SKY, currentPos) - lightSubtract;

            if (currentValue > maxValue) {
                maxValue = currentValue;
            }

            if (maxValue >= 14) {
                return maxValue;
            }
        }

        currentPos.release();
        return maxValue;
    }

    boolean canSeeSky(BlockPos pos);

    int getEmittedLight(BlockPos pos, EnumSkyBlock type);

    default int getLightFromNeighbors(EnumSkyBlock type, BlockPos pos) {
        //TODO: use MutableBlockPos?
        int max = 0;
        for (EnumFacing direction : EnumFacing.values()) {
            int light = getLightFor(type, pos.offset(direction));
            if (light > max) {
                max = light;
            }
        }
        int decrease = Math.max(1, getBlockLightOpacity(pos));
        return Math.max(0, max - decrease);
    }

    void markEdgeNeedLightUpdate(BlockPos offset, EnumSkyBlock type);
}
