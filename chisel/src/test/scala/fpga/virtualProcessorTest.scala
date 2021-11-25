package fpga

import scala.util._
import chisel3.util._
import chisel3.iotesters._
import java.util.LinkedHashMap
import java.util.LinkedList


class VirtualProcessorTester(c: VirtualProcessor) extends PeekPokeTester(c) {
    /**
      * io.pipe: 
                ready_next: input
                ready_prev: output
                phv_in: input
                phv_out: output
                pause: input
        io.mod:
                gtw.en: input
                gtw.lut_entry_id: input
                gtw.lut_entry_proc_id: input
                gtw.lut_entry_config_id: input
                vsb.vs_id: input
                vsb.en: input
                vsb.addr: input
                vsb.data: input
      */

    poke(c.io.mod.gtw_mod.en, false)
    poke(c.io.mod.vsb_mod.vs_mod.en, false)
    poke(c.io.pipe.pause, false)

    poke(c.io.pipe.ready_next, true)
    poke(c.io.pipe.phv_in.valid, true)

    // 1 2 3 4 5 6 7 8
    val data0 = Seq(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
    for(i <- 0 until 8) {
        poke(c.io.pipe.phv_in.data(i), data0(i))
    }
    step(32)
    for (i <- 0 until 8) {
        printf("phv_out.data(%d) is: %d\n", i, peek(c.io.pipe.phv_out.data(i)))
    }
    // 0 2 3 4 5 6 7 8

    poke(c.io.pipe.pause, true)
    poke(c.io.mod.vsb_mod.vs_mod.en, true)
    poke(c.io.mod.vsb_mod.vs_id, 30)
    poke(c.io.mod.vsb_mod.vs_mod.addr, 7)
    poke(c.io.mod.vsb_mod.vs_mod.data, 11)

    step(1)
    poke(c.io.pipe.pause, false)
    val data1 = Seq(0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01)
    for(i <- 0 until 8) {
        poke(c.io.pipe.phv_in.data(i), data1(i))
    }
    step(32)
    for (i <- 0 until 8) {
        printf("phv_out.data(%d) is: %d\n", i, peek(c.io.pipe.phv_out.data(i)))
    }
    // 0 7 6 5 4 3 2 11
}

object VirtualProcessorTesterGen extends App {
    chisel3.iotesters.Driver.execute(args, () => new VirtualProcessor)(c => new VirtualProcessorTester(c))
}