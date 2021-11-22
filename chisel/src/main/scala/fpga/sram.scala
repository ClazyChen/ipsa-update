package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

// double-port SRAM
// 1 port only used for R and
// the other port only used for W

// ADD : support R&W at the same time

// does not support masked write

class SRAMWritePort(addr_width: Int, data_width: Int) extends Bundle {
    val en = Input(Bool())
    val addr = Input(UInt(addr_width.W))
    val data = Input(UInt(data_width.W))
}

class SRAMReadPort(addr_width: Int, data_width: Int) extends Bundle {
    val en = Input(Bool())
    val addr = Input(UInt(addr_width.W))
    val data = Output(UInt(data_width.W))
}

class SRAM(capacity: Int, data_width: Int) extends Module {
    val addr_width = log2Ceil(capacity)
    val io = IO(new Bundle {
        val w = new SRAMWritePort(addr_width, data_width)
        val r = new SRAMReadPort(addr_width, data_width)
    })

    val mem = SyncReadMem(capacity, UInt(data_width.W))
    io.r.data := DontCare
    when (io.w.en) {
        mem.write(io.w.addr, io.w.data)
    }
    when (io.r.en) {
        io.r.data := mem.read(io.r.addr)
    }
}

// object SRAM_OBJ extends App {
//     (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new SRAM(256, 64))))
// }