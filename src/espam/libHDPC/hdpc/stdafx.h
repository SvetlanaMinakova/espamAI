/* $Id: stdafx.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <boost/static_assert.hpp>
#pragma warning(disable: 4996)
#include <boost/array.hpp>
#pragma warning(default: 4996)
#include <boost/cstdint.hpp> 

#define lengthof(x) (sizeof(x)/sizeof(x[0]))
#define endof(x) (&x[lengthof(x)])
#define lastof(x) (&x[lengthof(x) - 1])

using boost::uint16_t;
using boost::uint32_t;
using boost::int32_t;
using boost::uint64_t;
BOOST_STATIC_ASSERT(sizeof(uint16_t) == 2);
BOOST_STATIC_ASSERT(sizeof(int32_t)  == 4);
BOOST_STATIC_ASSERT(sizeof(uint32_t) == 4);
BOOST_STATIC_ASSERT(sizeof(uint64_t) == 8);

#if defined(HDPC_DEBUG)
#define HDPC_DEBUG_MODE 1
#else
#define HDPC_DEBUG_MODE 0
#endif /* HDCP_DEBUG */
