package li.cil.oc.client.renderer.blockentity

object RenderUtil {
  def shouldShowErrorLight(hash: Int): Boolean = {
    val time = System.currentTimeMillis + hash
    val timeSlice = time / 500
    timeSlice % 2 == 0
  }
}
