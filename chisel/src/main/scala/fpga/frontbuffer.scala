package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class FrontBuffer extends Module {
    val io = IO(new Bundle {
        val pipe = new PipelinePause
    })

    val fifo = Module(new FIFO(const.FBUF.capacity, const.FBUF.width))
    fifo.io.enq.en := false.B
    fifo.io.enq.data := 0.U

    // enqueue
    io.pipe.ready_prev := fifo.io.enq.valid
    when (io.pipe.phv_in.valid) {
        val temp_data = Wire(Vec(const.FBUF.width/8, UInt(8.W)))
        for (j <- 0 until const.FBUF.width/8) {
            temp_data(j) := io.pipe.phv_in.data(j)
        }
        fifo.io.enq.en := true.B
        fifo.io.enq.data := temp_data.asUInt
    }

    // dequeue
    io.pipe.phv_out := 0.U.asTypeOf(new PHV)
    fifo.io.deq.en := ~io.pipe.pause
    when (fifo.io.deq.valid) {
        io.pipe.phv_out.valid := true.B
        for (j <- 0 until const.FBUF.width/8) {
            io.pipe.phv_out.data(j) := fifo.io.deq.data(j*8+7, j*8)
        }
    }
}

object FBUF_OBJ extends App {
    (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new FrontBuffer)))
}