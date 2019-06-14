// File automatically generated by ESPAM

#ifndef appMain_H
#define appMain_H
#include <stdlib.h>
#include <iostream>
#include <vector>
#include "csdfNode.h"
#include "appFunc.h"
#include "types.h"

class appMain {
public:
  appMain();
  virtual ~appMain();

// list of available nodes
  static std::map< std::string,csdfNode* > nodes;

  void main();

// Call of the specific node 
  static void run_node(std::string node_name);
};
#endif // appMain_H
