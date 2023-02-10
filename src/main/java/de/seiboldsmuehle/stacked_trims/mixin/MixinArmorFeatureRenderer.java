package de.seiboldsmuehle.stacked_trims.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ArmorFeatureRenderer.class)
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    public MixinArmorFeatureRenderer(FeatureRendererContext<T, M> context, SpriteAtlasTexture armorTrimsAtlas) {
        super(context);
        this.armorTrimsAtlas = armorTrimsAtlas;
    }

    @Shadow
    private final SpriteAtlasTexture armorTrimsAtlas;

    @Redirect(method="renderArmor", at = @At(value="INVOKE", target = "Lnet/minecraft/item/trim/ArmorTrim;getTrim(Lnet/minecraft/registry/DynamicRegistryManager;Lnet/minecraft/item/ItemStack;)Ljava/util/Optional;"))
    public Optional<ArmorTrim> getTrim(DynamicRegistryManager registryManager, ItemStack stack) {
        if (stack.isIn(ItemTags.TRIMMABLE_ARMOR) && stack.getNbt() != null && stack.getNbt().contains("Trim")) {
            NbtList nbtList = stack.getNbt().getList("Trim",10);
            DataResult var10000 = ArmorTrim.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, registryManager), nbtList.get(0));
            ArmorTrim armorTrim = (ArmorTrim)var10000.result().orElse((Object)null);
            return Optional.ofNullable(armorTrim);
        } else {
            return Optional.empty();
        }
    }

    @Shadow
    private void renderTrim(ArmorMaterial armorMaterial, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ArmorTrim armorTrim, boolean bl, A bipedEntityModel, boolean bl2, float f, float g, float h) {
        Sprite sprite = armorTrimsAtlas.getSprite(bl2 ? armorTrim.getLeggingsModelId(armorMaterial) : armorTrim.getGenericModelId(armorMaterial));
        VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumerProvider, TexturedRenderLayers.getArmorTrims(), true, bl));
        bipedEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, f, g, h, 1.0F);
    }

    //METHOD CAUSES INFINITE LOOT CYCLE -> "locals = LocalCapture.CAPTURE_FAILSOFT" is Broken, find other way to change if content or fix capture
/*
    @Inject(method="renderArmor", at = @At(value="INVOKE", target = "Lnet/minecraft/world/World;getEnabledFeatures()Lnet/minecraft/resource/featuretoggle/FeatureSet;"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void mixinRenderTrim(CallbackInfo ci, MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, ItemStack itemStack, ArmorItem armorItem, boolean bl, boolean bl2){
        if (entity.world.getEnabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
            ArmorTrim.getTrim(entity.world.getRegistryManager(), itemStack).ifPresent((armorTrim) -> {
                renderTrim(armorItem.getMaterial(), matrices, vertexConsumers, light, armorTrim, bl2, model, bl, 1.0F, 1.0F, 1.0F);
            });
        }
        ci.cancel();
    }
    */
}
