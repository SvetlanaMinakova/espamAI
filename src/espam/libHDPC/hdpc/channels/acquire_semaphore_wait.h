/* $Id: acquire_semaphore_wait.h,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/channels/acquire_wait.h>
#include <hdpc/channels/semaphore_wait.h>

namespace hdpc { 
	namespace channel { 
		namespace lock {

			HDPC_CHANNEL_LOCK_ACQUIRE(SEMAPHORE)

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
