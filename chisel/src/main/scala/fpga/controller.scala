package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

control the INSERT / DELETE of TSPs

*/

class Controller extends Module {
    val capacity = const.CTRL.update_item_capacity
    val item_width = const.CTRL.update_item_width
    val addr_width = log2Ceil(capacity)

    val io = IO(new Bundle {
        val end = Input(Bool()) // complete the update sequence signal
        val enq = new FIFOEnqueuePort(item_width) // update instruction
    })

    val fifo = Module(new FIFO(capacity, item_width))
    fifo.io.enq <> io.enq
    fifo.io.deq := false.B

    val sEnq :: sDeq :: sWait :: Nil = Enum(3)
    val status = RegInit(sWait)

    switch (status) {
        is (sWait) {
            when (io.enq.en) {
                status := sEnq // start update
            }
        }
        is (sEnq) {
            when (io.end) {
                status := sDeq // end update
            }
        }
        is (sDeq) {
            fifo.io.deq.en := true.B
            when (fifo.io.deq.valid) {
                // TODO : read a update instruction and do some operation
            } .otherwise {
                status := sWait
            }
        }
    }

}