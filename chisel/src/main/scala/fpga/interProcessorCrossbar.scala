package fpga

import chisel3._
import chisel3.util._

class CrossbarModify(nInput: Int, nOutput: Int) extends Bundle {
    val en = Input(Bool())
    val input_id = Input(UInt(log2Ceil(nInput).W))
    val output_onehot = Input(UInt(nOutput.W))
}

class Crossbar(nInput: Int, nOutput: Int) extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val xbar_mod = new CrossbarModify(nInput, nOutput)
    })

    // naive - full crossbar

    // crosspoints[i][j] = edge(i -> j)
    val crosspoints = Reg(Vec(const.processor_number, UInt(const.processor_number.W)))

}