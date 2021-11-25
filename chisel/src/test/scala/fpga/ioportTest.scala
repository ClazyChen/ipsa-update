package fpga

import scala.util._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class InPortTester(c: InPort) extends PeekPokeTester(c) {
    
    poke(c.io.port_in.data, 0x1000100010001000L)
    poke(c.io.port_in.last, true)
    poke(c.io.port_in.valid, true)
    poke(c.io.enq_data.valid, true)
    poke(c.io.enq_last.valid, true)

    step(1)
    val reg = Seq(0, 16, 0, 16, 0, 16, 0, 16)
    expect(c.io.phv_out.valid, true)
    expect(c.io.enq_data.en, true)
    for (i <- 0 until 8){
        expect(c.io.phv_out.data(i), reg(i))
    }

    step(1)
    poke(c.io.port_in.data, 0x1101011110111001L)
    poke(c.io.port_in.last, true)
    poke(c.io.port_in.valid, true)
    poke(c.io.enq_data.valid, true)
    poke(c.io.enq_last.valid, true)

    step(1)
    val reg1 = Seq(0x01, 0x10, 0x11, 0x10, 0x11, 0x01, 0x01, 0x11)
    expect(c.io.phv_out.valid, true)
    expect(c.io.enq_data.en, true)
    for (i <- 0 until 8){
        expect(c.io.phv_out.data(i), reg1(i))
    }
    
}

object InPortTesterGen extends App {
    chisel3.iotesters.Driver.execute(args, () => new InPort())(c => new InPortTester(c))
}