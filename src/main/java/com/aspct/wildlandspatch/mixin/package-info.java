/**
 * Mixins, one class per target.
 *
 * <p>Conventions for this package:
 *
 * <ul>
 *   <li>Name a mixin after the class it targets, plus the owner when the name is ambiguous:
 *       {@code LivingEntityMixin}, {@code SomeModBlockEntityMixin}.</li>
 *   <li>Client only mixins go in {@code mixin.client} and are listed under {@code "client"} in
 *       wildlands_patch.mixins.json, so they are never applied on a dedicated server.</li>
 *   <li>Every mixin targeting another mod is listed in that same file and must be paired with a
 *       fix toggle in {@link com.aspct.wildlandspatch.Config}, checked inside the handler rather
 *       than around the injection.</li>
 *   <li>Prefer an access transformer to a mixin when all that is needed is visibility.</li>
 *   <li>Keep handlers short and delegate to a normal class. Mixin bodies are the hardest code in
 *       the project to debug and the easiest to break on a mod update.</li>
 * </ul>
 */
package com.aspct.wildlandspatch.mixin;
