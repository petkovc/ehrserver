/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.openehr.opt.manager.OptManager
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import org.springframework.web.context.request.RequestContextHolder

class DataCriteriaDV_ORDINAL extends DataCriteria {

   static String indexType = 'DvOrdinalIndex'

    // Comparison values
    List valueValue // int
    List symbol_codeValue // coded text
    String symbol_terminology_idValue // coded text
    String symbol_valueValue // text

    // Comparison operands
    String valueOperand
    String symbol_valueOperand
    String symbol_codeOperand
    String symbol_terminology_idOperand

    boolean valueNegation = false
    boolean symbol_valueNegation = false
    boolean symbol_codeNegation = false
    boolean symbol_terminology_idNegation = false

    DataCriteriaDV_ORDINAL()
    {
       rmTypeName = 'DV_ORDINAL'
       alias = 'dvol'
    }

    static hasMany = [valueValue: Integer, symbol_codeValue: String]

    static constraints = {
       valueOperand(nullable:true)
       symbol_valueOperand(nullable:true)
       symbol_codeOperand(nullable:true)
       symbol_terminology_idOperand(nullable:true)
       symbol_terminology_idValue(nullable:true)
       symbol_valueValue(nullable:true)
    }
    static mapping = {
       symbol_terminology_idValue column: "dv_ordinal_terminology_id"
       symbol_valueValue column: "dv_ordinal_value"
    }

   /**
    * Metadata that defines the types of criteria supported to search
    * by conditions over DV_CODED_TEXT.
    * @return
    */
   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      def spec = [
         [ // for the ordinal number
            value: [
               eq:  'value', // operands eq,lt,gt,... can be applied to attribute magnitude and the reference value is a single value
               lt:  'value',
               gt:  'value',
               neq: 'value',
               le:  'value',
               ge:  'value',
               between: 'range'
            ]
         ]
         /*, // REMOVED the other criteria because it only make sense to query by the ordinal value and get the code, not query by the code.
         [ // like dv text
            symbol_value: [
               contains:  'value', // ilike %value%
               eq:  'value'
            ]
         ],
         [ // like coded text
            symbol_code: [
               eq: 'value',    // operand eq can be applied to attribute code and the reference value is a single value
               in_list: 'list', // operand in_list can be applied to attribute code and the reference value is a list of values
               in_snomed_exp: 'snomed_exp'
            ],
            symbol_terminology_id: [
               eq: 'value',
               contains: 'value'
            ]
         ]
         */
      ]

      if (returnCodes)
      {
         def optMan = OptManager.getInstance()
         def codes = [:]
         def lang = RequestContextHolder.currentRequestAttributes().session.lang
         def namespace = RequestContextHolder.currentRequestAttributes().session.organization.uid
         def constraint = optMan.getNode(archetypeId, path, namespace)

         if (constraint.type == 'C_DV_ORDINAL')
         {
            constraint.list.each { cdvord_item ->

               //println cdvord_item.value +" "+ cdvord_item.symbol.codeString +" "+ cdvord_item.symbol.terminologyId // int, CodePhrase
               /*
               codes[cdvord_item.value] = [
                  code: cdvord_item.symbol.codeString,
                  name: optMan.getText(archetypeId, cdvord_item.symbol.codeString, lang, namespace),
                  terminologyId: cdvord_item.symbol.terminologyId // instead of putting the terminology appart from the code, this needs both and value to be on the same structure, the GUI should handle this case for displaying
               ]
               */
               codes[cdvord_item.value] = optMan.getText(archetypeId, cdvord_item.symbol.codeString, lang, namespace)
            }

            spec[0].value.codes = codes
         }
      }

      return spec
   }

   static List attributes()
   {
      return ['value', 'symbol_value', 'symbol_code', 'symbol_terminology_id']
   }

   static List functions()
   {
      return []
   }

   boolean containsFunction()
   {
      return false
   }
}
