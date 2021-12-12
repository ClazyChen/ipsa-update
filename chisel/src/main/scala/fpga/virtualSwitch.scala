package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

a virtual switch with:
core architecture (proc, xbar, sram)
front buffer & fifo & controller

NOW only IPSA arch + controller + FBUF

*/



class VirtualSwitch extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val pcie = new PcieBramInPort
    })

    val fbuf = Module(new FrontBuffer)
    val arch = Module(new VirtualArchitecture)
    (io.pipe >> fbuf.io.pipe) ~ arch.io.pipe << io.pipe
 
    val schd = Module(new Scheduler)
    schd.io.pcie := io.pcie
    fbuf.io.pipe.pause := schd.io.ctrl.pause_fbuf
    arch.io.pause := schd.io.ctrl.pause
    arch.io.mod := schd.io.ctrl.mod

}