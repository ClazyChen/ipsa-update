package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**
gateway can be processed in only a cycle
because we use LUT instead of SRAM
*/

class VirtualGatewayModify extends Bundle {
    val en                = Input(Bool())
    val lut_entry_id      = Input(UInt(const.GTW.lut_entry_id_width.W))
    val lut_entry_proc_id = Input(UInt(const.processor_id_width.W))
    val lut_entry_config_id = Input(UInt(const.config_id_width.W))
}

class VirtualGateway extends Module {
    val io = IO(new Bundle {
        val pipe = new PipelinePause
        val mod  = new VirtualGatewayModify
        // val gtw_in = Input(UInt(const.GTW.lut_entry_id_width.W))
    })
    
    val (phv, release) = init.pipeline(io.pipe)
    val proc_id_lut = Mem(const.GTW.lut_capacity, UInt(const.processor_id_width.W))
    val config_id_lut = Mem(const.GTW.lut_capacity, UInt(const.config_id_width.W))

    val gtw_in = io.pipe.phv_in.data(const.PROC.cycle_number) // use data[32] as key
    val gtw = init.latch(gtw_in, release)
    io.pipe.phv_out.next_processor_id := proc_id_lut(gtw)
    io.pipe.phv_out.next_config_id := config_id_lut(gtw)

    // modification ignores pipeline pausing
    when (io.mod.en) {
        proc_id_lut(io.mod.lut_entry_id) := io.mod.lut_entry_proc_id
        config_id_lut(io.mod.lut_entry_id) := io.mod.lut_entry_config_id
    }
}
