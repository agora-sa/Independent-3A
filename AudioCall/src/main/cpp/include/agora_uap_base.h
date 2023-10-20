//
//  Agora Uplink Audio Processing Library
//
//  Copyright (c) 2023 Agora IO. All rights reserved.
//
//  This program is confidential and proprietary to Agora.io.
//  And may not be copied, reproduced, modified, disclosed to others, published
//  or used, in whole or in part, without the express prior written permission
//  of Agora.io.

#pragma once

namespace AgoraUAP {

/**
 * The UAP error codes.
 */
enum ErrorCode {
  kNoError = 0,
  kUnspecifiedError = -1,
  kCreationFailedError = -2,
  kUnsupportedComponentError = -3,
  kUnsupportedFunctionError = -4,
  kNullPointerError = -5,
  kBadParameterError = -6,
  kBadSampleRateError = -7,
  kBadDataLengthError = -8,
  kBadNumberChannelsError = -9,
  kFileError = -10,
  kStreamParameterNotSetError = -11,
  kNotEnabledError = -12,

  // Licensing
  kNoServerResponseError = -104,
  kCertRawError = -157,
  kCertJsonPartError = -158,
  kCertJsonInvalError = -159,
  kCertJsonNomemError = -160,
  kCertCustomError = -161,
  kCertCredentialError = -162,
  kCertSignError = -163,
  kCertFailError = -164,
  kCertBufError = -165,
  kCertNULLError = -166,
  kCertDuedateError = -167,
};

}  // namespace AgoraUAP
