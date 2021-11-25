package fpga

import scala.util._
import chisel3.util._
import chisel3.iotesters._
import java.util.LinkedHashMap
import java.util.LinkedList


class VirtualStageTester(c: VirtualStage) extends PeekPokeTester(c) {
    /**
      * io.pipe: 
                ready_next: input
                ready_prev: output
                phv_in: input
                phv_out: output
        io.mod:
                en: input
                addr: input
                data: input
      */

    val randNum = new Random
    
    poke(c.io.pipe.ready_next, true)
    poke(c.io.pipe.phv_in.valid, true)
    poke(c.io.pipe.pause, false)
    poke(c.io.mod.en, false)
    val data0 = Seq(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
    for(i <- 0 until 8) {
        poke(c.io.pipe.phv_in.data(i), data0(i))
    }

    step(1)
    expect(c.io.pipe.ready_prev, true)
    // assign io_pipe_phv_out_data_0 = 7'h0 == addr ? data : phv_data_0;
    // thus phv_out_data_0 = 0 not 1
    for (i <- 0 until 8) {
        printf("phv_out.data(%d) is: %d\n", i, peek(c.io.pipe.phv_out.data(i)))
        // '0' 2 3 4 5 6 7 8 ?
    }

    /***************** next phv **********************/
    
    val data1 = Seq(0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01)
    for(i <- 0 until 8) {
        poke(c.io.pipe.phv_in.data(i), data1(i))
    }

    step(1)
    for (i <- 0 until 8) {
        printf("phv_out.data(%d) is: %d\n", i, peek(c.io.pipe.phv_out.data(i)))
        // '0' 7 6 5 4 3 2 1
    }

    /******************* next phv **************************/
    poke(c.io.mod.en, true)
    poke(c.io.mod.addr, 3)
    poke(c.io.mod.data, 10)
    
    step(1)
    // assign io_pipe_phv_out_data_0 = 7'h0 == addr ? data : phv_data_0;
    // addr = 3, thus phv_out_data_0 = phv_data_0 = 1
    for (i <- 0 until 8) {
        printf("phv_out.data(%d) is: %d\n", i, peek(c.io.pipe.phv_out.data(i)))
        // 8 7 6 10 4 3 2 1
    }
}

object VirtualStageTesterGen extends App {
    chisel3.iotesters.Driver.execute(args, () => new VirtualStage)(c => new VirtualStageTester(c))
}