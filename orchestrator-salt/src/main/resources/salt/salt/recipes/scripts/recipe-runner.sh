#!/bin/bash

function run_recipe() {
  local recipe-type="$1"
  local recipe-name="$2"

  mkdir -p /opt/scripts/${recipe-type}
  sh -x /opt/scripts/${recipe-type}/${recipe-name} 2>&1 | tee -a /var/log/recipes/${recipe-type}/${recipe-name}.log

  local EXIT_CODE=${PIPESTATUS[0]}

  if [[ ${EXIT_CODE} -eq 0 ]]; then
    echo $(date) >> /var/log/recipes/${recipe-type}/${recipe-name}.success
  fi

}
