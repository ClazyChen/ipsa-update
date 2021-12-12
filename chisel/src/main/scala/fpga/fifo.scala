package fpga

import chisel3._
import chisel3.util._

class FIFOEnqueuePort(width: Int) extends Bundle {
    val en    = Input(Bool())
    val data  = Input(UInt(width.W))
    val valid = Output(Bool()) // if full, valid = false
}

class FIFODequeuePort(width: Int) extends Bundle {
    val en    = Input(Bool())
    val data  = Output(UInt(width.W))
    val valid = Output(Bool()) // if empty, valid = false
}

class FIFO(capacity: Int, width: Int) extends Module {
    val io = IO(new Bundle {
        val enq = new FIFOEnqueuePort(width)
        val deq = new FIFODequeuePort(width)
        // val empty = Output(Bool())
    })

    val sram = Module(new SRAM(capacity, width))
    val addr_width = log2Ceil(capacity)
    val front = RegInit(0.U(addr_width.W))
    val back  = RegInit(0.U(addr_width.W))
    
    val next_front = front + 1.U(addr_width.W)
    val next_back = back + 1.U(addr_width.W)
    val nfull  = front =/= next_back
    val nempty = front =/= back

    // enqueue interface
    val wen = nfull && io.enq.en
    sram.io.w.en   := wen
    sram.io.w.addr := back
    sram.io.w.data <> io.enq.data
    when (wen) {
        back := next_back
    }

    // dequeue interface
    val ren = nempty && io.deq.en
    sram.io.r.en   := ren
    sram.io.r.addr := front
    sram.io.r.data <> io.deq.data
    when (ren) {
        front := next_front
    }

    val deq_valid = RegNext(ren)
    io.enq.valid := nfull
    io.deq.valid := deq_valid // wait a cycle and get the output
    // io.empty := ~nempty
}