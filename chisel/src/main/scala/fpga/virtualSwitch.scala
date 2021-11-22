package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

a virtual switch with:
core architecture (proc, xbar, sram)
front buffer & fifo & controller

*/



class VirtualSwitch extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
    })

}