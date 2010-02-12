/* $Id: process.hpp,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <sstream>
#include <iomanip>

#include <hdpc/stdafx.h>
#include <hdpc/debug.hpp>
#include <hdpc/timer.hpp>
#include <hdpc/core_stuff.hpp>
#include <hdpc/platforms/platform.h>
#include <hdpc/channels/channel.h>

namespace hdpc {

	using namespace channel;
	using platform::Platform;

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT> class Process;

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT> class Exec {
	public:
		virtual void operator()(hdpc::Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>&) = 0;
	};

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT> class Process {
	public:
		enum readError {READ_FINISHED, READ_ERROR};
		typedef Exec<PLATFORM, INPORT_COUNT, OUTPORT_COUNT> EXEC;
		//BOOST_STATIC_ASSERT(__is_base_of(Platform, PLATFORM));
		//BOOST_STATIC_ASSERT(INPORT_COUNT != 0 || OUTPORT_COUNT != 0);

		Process();
		~Process();

		template <lock::lock_t lock_t, typename token_t> bool attachinput(size_t port_in, ChannelBase *&q, size_t queueSize);
		template <lock::lock_t lock_t, typename token_t> bool attachoutput(size_t port_out, ChannelBase *&p, size_t queueSize);

		boost::thread* start(cpu_set_t, PLATFORM &a, EXEC& process, bool allocmem = false);
		inline void inc_execution_cntr();
		void waitforfinish();

		template <lock::lock_t lock_t, typename token_t> inline bool readFromPort(size_t port, token_t &element);
		template <lock::lock_t lock_t, typename token_t> inline bool writeToPort(size_t port, const token_t &element);

		template <lock::lock_t lock_t, typename token_t> inline const token_t &getReadPointer(size_t port);
		template <lock::lock_t lock_t, typename token_t> inline token_t &getWritePointer(size_t port);
		template <lock::lock_t lock_t, typename token_t> inline void releaseReadPointer(size_t port);
		template <lock::lock_t lock_t, typename token_t> inline void releaseWritePointer(size_t port);

		inline Platform &getProcess() const;
		inline ChannelBase *&getInPort(size_t port);
		inline ChannelBase *&getOutPort(size_t port);
		inline void *getDeviceInMem(size_t port);
		inline void *getDeviceOutMem(size_t port);

	private:
		template <lock::lock_t lock_t, typename token_t> inline Channel<token_t, lock_t>* getPort(ChannelBase *port);

		bool allocMemToProcess();
		bool freeMemFromProcess();
		bool checkConnections();
		void finish(std::stringstream& buf);

		static void executeIntermediate(Process* p);

		PLATFORM* callback;
		EXEC* do_work;
		bool use_device_memory;
		void* __write_ptr;
		size_t execution_count;
		boost::thread* th;

		boost::array<ChannelBase*, INPORT_COUNT > inPorts;       /* pointers to input channels between processes */
		boost::array<ChannelBase*, OUTPORT_COUNT> outPorts;      /* pointers to output channels between processes */
		boost::array<int,          INPORT_COUNT > in;            /* count of elements in input channel so we know how many to release when releasePorts() is called */
		boost::array<int,          OUTPORT_COUNT> out;           /* count of elements in output channel so we know how many to release when releasePorts() is called */
		boost::array<void*,        INPORT_COUNT > local_mem_in;  /* pointers to device memory if used */
		boost::array<void*,        OUTPORT_COUNT> local_mem_out; /* pointers to device memory if used */
	};

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::Process(): use_device_memory(false), execution_count(0) {
		inPorts.assign(NULL);
		outPorts.assign(NULL);

		in.assign(NULL);
		out.assign(NULL);

		local_mem_in.assign(NULL);
		local_mem_out.assign(NULL);
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::~Process() {
		for (size_t i = 0; i != outPorts.size(); i++) {
			delete outPorts[i];
			outPorts[i] = NULL;
		}
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::attachinput(size_t port_in, ChannelBase *&q, size_t queueSize) {
		ChannelBase *&p = getInPort(port_in);
		if (p != NULL || q != NULL) debug::error("input port %d is already connected", port_in);
		p = q = new Channel<token_t, lock_t>(queueSize);
		return p != NULL;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::attachoutput(size_t port_out, ChannelBase *&p, size_t queueSize) {
		ChannelBase *&q = getOutPort(port_out);
		if (p != NULL || q != NULL) debug::error("output port %d is already connected", port_out);
		q = p = new Channel<token_t, lock_t>(queueSize);
		return q != NULL;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	inline Platform &Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getProcess() const {
		return *callback;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	ChannelBase *&Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getInPort(size_t port)  {
		return inPorts[port];
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	ChannelBase *&Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getOutPort(size_t port) {
		return outPorts[port];
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	void *Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getDeviceInMem(size_t port)  {
		return local_mem_in[port];
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	void *Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getDeviceOutMem(size_t port) {
		return local_mem_out[port];
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	boost::thread* Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::start(cpu_set_t cpu_mask, PLATFORM &a, EXEC& process, bool allocmem)  {
		if (!checkConnections()) throw boost::thread_resource_error();
		callback = &a;
		do_work  = &process;

		use_device_memory = allocmem;
		th = new boost::thread(executeIntermediate, this);

		setThreadAffinity(th->native_handle(), cpu_mask);
		return th;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::readFromPort(size_t port, token_t &element) {
		Channel<token_t, lock_t> *p = getPort<lock_t, token_t>(getInPort(port));
		p->read(element);
		return true;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::writeToPort(size_t port, const token_t &element) {
		Channel<token_t, lock_t> *q = getPort<lock_t, token_t>(getOutPort(port));
		q->write(element);
		return true;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	const token_t &Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getReadPointer(size_t port) {
		Channel<token_t, lock_t> *p = getPort<lock_t, token_t>(getInPort(port));
		const token_t *ptr = &p->acquire_read_ptr();
		in[port]++;

		if (use_device_memory) {
			callback->write(local_mem_in[port], ptr, sizeof(*ptr));
			ptr = static_cast<const token_t*>(local_mem_in[port]);
		}
		return *ptr;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	token_t &Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getWritePointer(size_t port) {
		Channel<token_t, lock_t> *q = getPort<lock_t, token_t>(getOutPort(port));
		token_t *ptr = &q ->acquire_write_ptr();
		out[port]++;

		if (use_device_memory) {
			__write_ptr = ptr;
			ptr = static_cast<token_t*>(local_mem_out[port]);
		}
		return *ptr;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	void Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::releaseReadPointer(size_t port) {
		Channel<token_t, lock_t> *p = getPort<lock_t, token_t>(getInPort(port));
		p->release_read_ptr();
		in[port]--;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	void Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::releaseWritePointer(size_t port) {
		Channel<token_t, lock_t> *q = getPort<lock_t, token_t>(getOutPort(port));

		if (use_device_memory) {
			callback->read(local_mem_out[port], __write_ptr, sizeof(token_t));
			__write_ptr = NULL;
		}
		q->release_write_ptr();
		out[port]--;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	template <lock::lock_t lock_t, typename token_t>
	Channel<token_t, lock_t>* Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::getPort(ChannelBase *port) {
		if (HDPC_DEBUG_MODE) {
			Channel<token_t, lock_t> *p = dynamic_cast<Channel<token_t, lock_t>*>(port);
			if (p == NULL) {
				debug::error("cannot derive port-type");
				throw READ_ERROR;
			}
			return p;
		}

		return static_cast<Channel<token_t, lock_t>*>(port);
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::allocMemToProcess() {
		if (!use_device_memory) return true;

		try {
			for (size_t i = 0; i != inPorts.size(); i++) {
				size_t size = getInPort(i)->get_sizeof() * getInPort(i)->get_length();
				if (!callback->allocmem(size, local_mem_in[i])) throw;
			}

			for (size_t i = 0; i != outPorts.size(); i++) {
				size_t size = getOutPort(i)->get_sizeof() * getOutPort(i)->get_length();
				if (!callback->allocmem(size, local_mem_out[i])) throw;
			}
		} catch (...) {return false;}
		return true;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::freeMemFromProcess() {
		try {
			for (size_t i = 0; i != local_mem_in.size(); i++) {
				if (!callback->freemem(local_mem_in[i])) throw;
			}
			for (size_t i = 0; i != local_mem_out.size(); i++) {
				if (!callback->freemem(local_mem_out[i])) throw;
			}
		} catch (...) {return false;}
		return true;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	bool Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::checkConnections() {
		if (!HDPC_DEBUG_MODE) return true;
		bool err = false;

		for (int i = 0; i != inPorts.size(); i++) {
			if (getInPort(i) == NULL) {
				err = true;
				debug::error("input port %d not connected", i);
			}
		}
		for (int i = 0; i != outPorts.size(); i++) {
			if (getOutPort(i) == NULL) {
				err = true;
				debug::error("output port %d not connected", i);
			}
		}

		return err == false;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	void Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::finish(std::stringstream& buf) {
		double time_spent = 0;

		size_t nof_reads  = 0;
		for (int i = 0; i != inPorts.size(); i++) {
			ChannelBase &c = *getInPort(i);
			c.finish();

			nof_reads  += c.t_read.count();
			time_spent += c.t_read.elapsed(0);
			buf << "  in_channel(" << i << " " << c.t_read.count() << "r " << c.t_write.count() << "w " << c.get_buf_usage() << "m " << c.get_sizeof() << "s " << c.get_length() << "l): ";
			buf << std::fixed << std::setprecision(4) <<
			       c.t_read.elapsed(0)  << "t " << c.t_read.elapsed(1)  << "w " << c.t_read.elapsed(0)  - c.t_read.elapsed(1)  << "e" << "  " <<
			       c.t_write.elapsed(0) << "t " << c.t_write.elapsed(1) << "w " << c.t_write.elapsed(0) - c.t_write.elapsed(1) << "e" << std::endl;
		}

		size_t nof_writes = 0;
		for (int i = 0; i != outPorts.size(); i++) {
			ChannelBase &c = *getOutPort(i);

			nof_writes += c.t_write.count();
			time_spent += c.t_write.elapsed(0);
		}

		buf << "proc summary: " << execution_count << "e " << nof_reads << "r/" << nof_writes << "w " <<
		    std::fixed << std::setprecision(4) << time_spent << "c" << std::endl;
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	void Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::inc_execution_cntr() {
	#if defined(HDPC_DEBUG_MODE)
		++execution_count;
	#endif /* HDPC_DEBUG_MODE */
	}

	template <class PLATFORM, int INPORT_COUNT, int OUTPORT_COUNT>
	void Process<PLATFORM, INPORT_COUNT, OUTPORT_COUNT>::executeIntermediate(Process* p) {
		Platform *pla = p->callback;
		timer_t t1, t2;

		t1.start();
		pla->init();
		p->allocMemToProcess();
		try	{
			t2.start();
			(*p->do_work)(*p);
			t2.stop();
		}	catch (readError e) {
			if (e == READ_ERROR) debug::error("%s failed with unknown error", pla->get_name());
			throw;
		}
		p->freeMemFromProcess();
		pla->deinit();
		t1.stop();

		std::stringstream buf;
		buf << pla->get_name() << ": " << std::fixed << std::setprecision(4) <<
			     t1.busy() << "t " << t2.busy() << "e" << std::endl;
		p->finish(buf);

		debug::debug(buf.rdbuf()->str().c_str());
	}

} /* namespace hdpc */
