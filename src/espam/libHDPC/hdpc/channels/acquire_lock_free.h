/* $Id: acquire_lock_free.h,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/channels/acquire_wait.h>
#include <hdpc/channels/lock_free.h>

namespace hdpc { 
	namespace channel { 
		namespace lock {

			HDPC_CHANNEL_LOCK_ACQUIRE(LOCK_FREE)

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
