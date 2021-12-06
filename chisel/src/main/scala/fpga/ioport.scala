package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class Port extends Bundle {
    val data = UInt(const.bits_per_cycle.W)
    val last = Bool()
    val valid = Bool()
}

// TODO: this in port only support 64B packet header
//       to make it extended to 128B, we should concat 2 cycles into 1

class InPort extends Module {
    val io = IO(new Bundle {
        val port_in = Input(new Port)
        val phv_out = Output(new PHV) // to front buffer
        val enq_data = Flipped(new FIFOEnqueuePort(const.bits_per_cycle))
        val enq_last = Flipped(new FIFOEnqueuePort(1))
    })

    val first = RegInit(true.B)
    when (io.port_in.last) {
        first := true.B
    } .otherwise {
        first := ~io.port_in.valid
    }

    io.phv_out := 0.U.asTypeOf(new PHV)
    when (first) {
        io.phv_out.valid := true.B
        for (j <- 0 until const.PHV.header_data_length) { // QUESTION : network byte order ??
            io.phv_out.data(j) := io.port_in.data((j+1)*8-1,j*8)
        }
    }
    
    io.enq_data.en := io.port_in.valid
    io.enq_data.data := io.port_in.data
    io.enq_last.en := io.port_in.valid
    io.enq_last.data := io.port_in.last
}

class OutPort extends Module {
    val io = IO(new Bundle {
        val deq_data = Flipped(new FIFODequeuePort(const.bits_per_cycle))
        val deq_last = Flipped(new FIFODequeuePort(1))
        val phv_in = Input(new PHV)
        val port_out = Output(new Port)
    })

    val phv = RegNext(io.phv_in)
    val sWait :: sStart :: sDeq :: Nil = Enum(3)
    val status = RegInit(sWait)

    io.port_out := 0.U.asTypeOf(new Port)
    switch (status) {
        is (sWait) {
            io.deq_data.en := io.phv_in.valid
            io.deq_last.en := io.phv_in.valid
            status := sStart
        }
        is (sStart) {
            io.port_out.valid := true.B
            io.port_out.last := io.deq_last.data
            io.port_out.data := phv.data
            when (io.deq_last.data(0)) { 
                when (io.phv_in.valid) { // continuous small packet
                    io.deq_data.en := true.B
                    io.deq_last.en := true.B
                } .otherwise { // discrete packet
                    status := sWait
                }
            } .otherwise { //  big packet
                io.deq_data.en := true.B
                io.deq_last.en := true.B
                status := sDeq
            }
        }
        is (sDeq) {
            io.port_out.valid := true.B
            io.port_out.last := io.deq_last.data
            io.port_out.data := io.deq_data.data
            when (io.deq_last.data(0)) {
                when (io.phv_in.valid) { // continuous packet
                    io.deq_data.en := true.B
                    io.deq_last.en := true.B
                    status := sStart
                } .otherwise { // discrete packet
                    status := sWait
                }
            }
        }
    }
}