package de.seiboldsmuehle.stacked_trims.mixin;

import de.seiboldsmuehle.stacked_trims.ArmorTrimList;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    @Shadow @Mutable @Final
    private final SpriteAtlasTexture armorTrimsAtlas;

    @Shadow protected abstract void renderTrim(ArmorMaterial material, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, A model, boolean leggings);

    @Shadow protected abstract void renderGlint(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model);

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context, SpriteAtlasTexture armorTrimsAtlas) {
        super(context);
        this.armorTrimsAtlas = armorTrimsAtlas;
    }

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/trim/ArmorTrim;getTrim(Lnet/minecraft/registry/DynamicRegistryManager;Lnet/minecraft/item/ItemStack;)Ljava/util/Optional;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void mixinRenderTrim(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci, ItemStack itemStack, ArmorItem armorItem, boolean bl) {
        ArmorTrimList.getTrims(entity.getWorld().getRegistryManager(), itemStack).ifPresent((armorTrims) -> {
            for (ArmorTrim armorTrim : armorTrims) {
                renderTrim(armorItem.getMaterial(), matrices, vertexConsumers, light, armorTrim, model, bl);
            }
        });
        if (itemStack.hasGlint()) {
            renderGlint(matrices, vertexConsumers, light, model);
        }
    }
}
