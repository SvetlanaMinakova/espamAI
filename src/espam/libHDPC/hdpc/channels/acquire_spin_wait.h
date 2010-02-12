/* $Id: acquire_spin_wait.h,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/channels/acquire_wait.h>
#include <hdpc/channels/spin_wait.h>

namespace hdpc { 
	namespace channel { 
		namespace lock {

			HDPC_CHANNEL_LOCK_ACQUIRE(SPIN)

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
