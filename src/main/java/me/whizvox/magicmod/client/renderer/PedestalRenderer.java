package me.whizvox.magicmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.whizvox.magicmod.common.block.entity.PedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {

  private final ItemRenderer itemRenderer;

  public PedestalRenderer(BlockEntityRendererProvider.Context context) {
    itemRenderer = context.getItemRenderer();
  }

  @Override
  public void render(PedestalBlockEntity pedestal, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    if (pedestal.hasItem()) {
      ItemStack stack = pedestal.getItem();
      pose.pushPose();
      pose.translate(0.5F, 1.2F, 0.5F);
      pose.scale(0.4F, 0.4F, 0.4F);
      float rotation = pedestal.getItemRotation(partialTicks);
      pose.mulPose(Axis.YP.rotation(rotation));
      itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, pose, buffer, pedestal.getLevel(), (int) pedestal.getBlockPos().asLong());
      pose.popPose();
    }
  }

}
